package org.graylog2.plugins.slack.output;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.outputs.MessageOutputConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugins.slack.SlackClient;
import org.graylog2.plugins.slack.SlackMessage;
import org.graylog2.plugins.slack.SlackPluginBase;
import org.graylog2.plugins.slack.StringReplacement;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

/** 
 * Call by Graylog when sending output to external source. 
 */
public class SlackMessageOutput extends SlackPluginBase implements MessageOutput {
  private AtomicBoolean running = new AtomicBoolean(false);

  private final Configuration configuration;
  private final Stream stream;

  private final SlackClient client;

  @Inject
  public SlackMessageOutput(@Assisted Stream stream, @Assisted Configuration configuration)
      throws MessageOutputConfigurationException {
    this.configuration = configuration;
    this.stream = stream;

    // Check configuration.
    try {
      checkConfiguration(configuration);
    } catch (ConfigurationException e) {
      throw new MessageOutputConfigurationException("Missing configuration: " + e.getMessage());
    }

    this.client = new SlackClient(configuration);

    running.set(true);
  }

  @Override
  public void stop() {
    running.set(false);
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public void write(Message msg) throws Exception {
    final String color = configuration.getString(CK_COLOR);
    SlackMessage message =
        new SlackMessage(
            buildMessage(stream, msg),
            configuration.getString(CK_CHANNEL),
            configuration.getString(CK_USER_NAME),
            configuration.getString(CK_MESSAGE_ICON),
            configuration.getBoolean(CK_LINK_NAMES));

    // Add attachments if requested.
    if (configuration.getBoolean(CK_ADD_STREAM_INFO)) {
      SlackMessage.Attachment attachment = message.addAttachment("Stream", color, null, null, null);
      attachment.addField(new SlackMessage.AttachmentField("Source", msg.getSource(), true));
      attachment.addField(
          new SlackMessage.AttachmentField("Stream Description", stream.getDescription(), false));
    }

    // If short mode is set, message to slack will be stripped down into very short information
    // If short mode is not set, message to slack will be composed as message attachment with short mode
    int count = configuration.getInt(CK_ADD_BLITEMS);
    if (!configuration.getBoolean(CK_SHORT_MODE) && count > 0) {
      SlackMessage.Attachment attachment = message.addAttachment(null, color, null, null, null);
      for (Map.Entry<String, Object> field : msg.getFields().entrySet()) {
        if (Message.RESERVED_FIELDS.contains(field.getKey())) {
          continue;
        }
        attachment.addField(
            new SlackMessage.AttachmentField(field.getKey(), field.getValue().toString(), true));
      }
    }

    // Send message to Slack
    try {
      client.send(message);
    } catch (SlackClient.SlackClientException e) {
      throw new RuntimeException("Could not send message to Slack.", e);
    }
  }

  /**
   * Create a slack <code>text</code> message
   *
   * @param stream a Graylog stream
   * @param mes a Graylog message
   * @return a text to be used in Slack message
   */
  private String buildMessage(Stream stream, Message msg) {
    if (configuration.getBoolean(CK_SHORT_MODE)) {
      return msg.getTimestamp()
              .toDateTime(DateTimeZone.getDefault())
              .toString(DateTimeFormat.shortTime())
          + ": "
          + msg.getMessage();
    }
    String graylogUri = configuration.getString(CK_GRAYLOG2_URL);
    String notifyUser = configuration.getString(CK_NOTIFY_USER);

    StringBuilder message = new StringBuilder();
    if (!isNullOrEmpty(notifyUser)) {
      notifyUser = StringReplacement.replaceWithPrefix(notifyUser, "@", msg.getFields());
      message.append(notifyUser.trim()).append(' ');
    }
    message.append(msg.getMessage());
    if (!isNullOrEmpty(graylogUri)) {
      message
          .append('<')
          .append(buildStreamLink(graylogUri, stream))
          .append('|')
          .append(stream.getTitle())
          .append('>');
    } else {
      message.append('_').append(stream.getTitle()).append('_');
    }
    return message.toString();
  }

  @Override
  public void write(List<Message> list) throws Exception {
    for (Message message : list) {
      write(message);
    }
  }

  public Map<String, Object> getConfiguration() {
    return configuration.getSource();
  }

  @FactoryClass
  public interface Factory extends MessageOutput.Factory<SlackMessageOutput> {
    @Override
    SlackMessageOutput create(Stream stream, Configuration configuration);

    @Override
    Config getConfig();

    @Override
    Descriptor getDescriptor();
  }

  @ConfigClass
  public static class Config extends MessageOutput.Config {
    @Override
    public ConfigurationRequest getRequestedConfiguration() {
      return configuration();
    }
  }

  public static class Descriptor extends MessageOutput.Descriptor {
    public Descriptor() {
      super("Slack Output", false, "", "Writes messages to a Slack chat room.");
    }
  }
}
