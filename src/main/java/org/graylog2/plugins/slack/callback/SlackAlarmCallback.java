package org.graylog2.plugins.slack.callback;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugins.slack.SlackClient;
import org.graylog2.plugins.slack.SlackMessage;
import org.graylog2.plugins.slack.SlackPluginBase;
import org.graylog2.plugins.slack.StringReplacement;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/** 
 * Call by Graylog when Alarm was occured. 
 */
public class SlackAlarmCallback extends SlackPluginBase implements AlarmCallback {
  private Configuration configuration;

  @Override
  public void initialize(final Configuration config) throws AlarmCallbackConfigurationException {
    this.configuration = config;
    try {
      checkConfiguration(config);
    } catch (ConfigurationException e) {
      throw new AlarmCallbackConfigurationException("Configuration error. " + e.getMessage());
    }
  }

  @Override
  public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {
    final SlackClient client = new SlackClient(configuration);
    final String color = configuration.getString(CK_COLOR);
    final String footerIconUrl = configuration.getString(CK_FOOTER_ICON_URL);
    final String footerText = configuration.getString(CK_FOOTER_TEXT);
    final String tsField = configuration.getString(CK_FOOTER_TS_FIELD);
    final String customFields = configuration.getString(CK_FIELDS);
    final boolean isAcknowledge = configuration.getBoolean(CK_ACKNOWLEDGE);
    final String graylogUri = configuration.getString(CK_GRAYLOG2_URL);
    final boolean isPreFormat = configuration.getBoolean(CK_PREFORMAT);
    // Create Message
    SlackMessage message =
        new SlackMessage(
            buildMessage(stream, result),
            configuration.getString(CK_CHANNEL),
            configuration.getString(CK_USER_NAME),
            configuration.getString(CK_MESSAGE_ICON),
            configuration.getBoolean(CK_LINK_NAMES));

    // Create Attachment for Stream section
    if (configuration.getBoolean(CK_ADD_STREAM_INFO)) {
      SlackMessage.Attachment attachment = message.addAttachment("Stream", color, null, null, null);
      attachment.addField(new SlackMessage.AttachmentField("Stream ID", stream.getId(), true));
      attachment.addField(
          new SlackMessage.AttachmentField("Stream Title", stream.getTitle(), false));
      attachment.addField(
          new SlackMessage.AttachmentField("Stream Description", stream.getDescription(), false));
    }

    // Create Attachment for Backlog and Fields section
    final List<Message> backlogItems = getAlarmBacklog(result);
    int count = configuration.getInt(CK_ADD_BLITEMS);
    if (count > 0) {
      final int blSize = backlogItems.size();
      if (blSize < count) {
        count = blSize;
      }
      boolean shortMode = configuration.getBoolean(CK_SHORT_MODE);
      final String[] fields;
      if (!isNullOrEmpty(customFields)) {
        fields = customFields.split(",");
      } else {
        fields = new String[0];
      }
      for (int i = 0; i < count; i++) {
        Message backlogItem = backlogItems.get(i);
        String footer = null;
        Long ts = null;
        if (!isNullOrEmpty(footerText)) {
          footer = StringReplacement.replace(footerText, backlogItem.getFields()).trim();
          if (!isNullOrEmpty(graylogUri))
            footer = new StringBuilder("<").append(buildMessageLink(graylogUri, backlogItem)).append('|').append(footer).append('>').toString();
          try {
            DateTime timestamp = null;
            if ("timestamp".equals(tsField)) { // timestamp is reserved field in org.graylog2.notifications.NotificationImpl
              timestamp = backlogItem.getTimestamp();
            } else {
              Object value = backlogItem.getField(tsField);
              if (value instanceof DateTime) {
                timestamp = (DateTime) value;
              } else {
                timestamp = new DateTime(value, DateTimeZone.UTC);
              }
            }
            ts = timestamp.getMillis() / 1000;
          } catch (NullPointerException | IllegalArgumentException e) {
            // ignore
          }
        }
        List<SlackMessage.Action> actionList = null;
        if (isAcknowledge) {
          actionList =
              Lists.newArrayList(
                  new SlackMessage.Action("acknowledge", "Acknowledge", "true", "primary"),
                  new SlackMessage.Action("decline", "It is not me!!", "true", "danger"));
        }
        StringBuilder backLogMessage = new StringBuilder(backlogItem.getMessage());
        if (isPreFormat)
          backLogMessage.insert(0, "```").append("```");
        final SlackMessage.Attachment attachment =
            message.addAttachment(
                backLogMessage.toString(),
                color,
                footer,
                footerIconUrl,
                ts,
                backlogItem.getId(),
                actionList);
        if (isPreFormat)
          attachment.setMarkdownIn("text");
        // Add custom fields from backlog list
        if (fields.length > 0) {
          Arrays.stream(fields)
              .map(String::trim)
              .forEach(f -> addField(backlogItem, f, shortMode, attachment));
        }
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
   * Collect all backlog from Alert result. 
   */
  protected List<Message> getAlarmBacklog(AlertCondition.CheckResult result) {
    final AlertCondition alertCondition = result.getTriggeredCondition();
    final List<MessageSummary> matchingMessages = result.getMatchingMessages();

    final int effectiveBacklogSize = Math.min(alertCondition.getBacklog(), matchingMessages.size());

    if (effectiveBacklogSize == 0) {
      return Collections.emptyList();
    }

    final List<MessageSummary> backlogSummaries = matchingMessages.subList(0, effectiveBacklogSize);
    final List<Message> backlog = Lists.newArrayListWithCapacity(effectiveBacklogSize);
    for (MessageSummary messageSummary : backlogSummaries) {
      Message message = messageSummary.getRawMessage();
      message.addField("gl2_document_index", messageSummary.getIndex());
      backlog.add(message);
    }

    return backlog;
  }

  /**
   * Shortcut method to add a backlog field into Slack attachment.
   *
   * @param message Graylog Message
   * @param fieldName field in backlog to be added
   * @param shortMode true to use Slack attachment short mode
   * @param attachment a Slack attachment object
   */
  private void addField(
      Message message, String fieldName, boolean shortMode, SlackMessage.Attachment attachment) {
    Object value = message.getField(fieldName);
    if (value != null) {
      attachment.addField(new SlackMessage.AttachmentField(fieldName, value.toString(), shortMode));
    }
  }

  /**
   * Create a slack <code>text</code> message from alert condition result.
   *
   * @param stream a Graylog stream
   * @param result a Graylog alert condition result
   * @return a text to be used in Slack message
   */
  private String buildMessage(Stream stream, AlertCondition.CheckResult result) {
    String graylogUri = configuration.getString(CK_GRAYLOG2_URL);
    String notifyUser = configuration.getString(CK_NOTIFY_USER);

    StringBuilder message = new StringBuilder();
    if (!isNullOrEmpty(notifyUser)) {
      List<MessageSummary> messageList = result.getMatchingMessages();
      if (messageList.size() > 0) {
        for (MessageSummary messageSummary : result.getMatchingMessages()) {
          notifyUser =
              StringReplacement.replaceWithPrefix(
                  notifyUser, "@", messageSummary.getRawMessage().getFields());
        }
      } else {
        notifyUser = StringReplacement.replace(notifyUser, Collections.emptyMap());
      }
      message.append(notifyUser.trim()).append(' ');
    }
    if (!isNullOrEmpty(graylogUri)) {
      message
          .append(" <")
          .append(buildStreamLink(graylogUri, stream))
          .append('|')
          .append(stream.getTitle())
          .append("> ");
    } else {
      message.append(" _").append(stream.getTitle()).append("_ ");
    }
    // Original Graylog message is too redundant. Try to make it short but it must compatible with all 3 Alerts type
    // message.append(result.getResultDescription());
    String description = result.getResultDescription();
    if (description != null) {
      message.append(description.replaceFirst("Stream", "").trim());
    }
    return message.toString();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return configuration.getSource();
  }

  @Override
  public void checkConfiguration() throws ConfigurationException {
    /* Never actually called by graylog-server */
  }

  @Override
  public ConfigurationRequest getRequestedConfiguration() {
    return configuration();
  }

  @Override
  public String getName() {
    return "Slack Alarm Callback";
  }
}
