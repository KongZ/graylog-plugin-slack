package org.graylog2.plugins.slack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

public class SlackPluginBase {
  public static final String CK_WEBHOOK_URL = "webhook_url";
  public static final String CK_CHANNEL = "channel";
  public static final String CK_USER_NAME = "user_name";
  public static final String CK_NOTIFY_USER = "notify_user";
  public static final String CK_ADD_STREAM_INFO = "add_stream_info";
  public static final String CK_SHORT_MODE = "short_mode";
  public static final String CK_LINK_NAMES = "link_names";
  public static final String CK_MESSAGE_ICON = "message_icon";
  public static final String CK_GRAYLOG2_URL = "graylog2_url";
  public static final String CK_PROXY_ADDRESS = "proxy_address";
  public static final String CK_COLOR = "color";
  public static final String CK_FIELDS = "custom_fields";
  public static final String CK_ADD_BLITEMS = "backlog_items";
  public static final String CK_FOOTER_TEXT = "footer_text";
  public static final String CK_FOOTER_ICON_URL = "footer_icon_url";
  public static final String CK_FOOTER_TS_FIELD = "ts_field";
  public static final String CK_ACKNOWLEDGE = "acknowledge";
  public static final String CK_TOKEN = "token";
  public static final String CK_PREFORMAT = "preformat";

  protected static ConfigurationRequest configuration() {
    final ConfigurationRequest configurationRequest = new ConfigurationRequest();

    configurationRequest.addField(
        new TextField(
            CK_WEBHOOK_URL,
            "Webhook URL",
            "",
            "Slack \"Incoming Webhook\" URL. This field is not required when using Slack token.",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_CHANNEL,
            "Channel",
            "#channel",
            "Name of Slack #channel or @user for a direct message.",
            ConfigurationField.Optional.NOT_OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_USER_NAME,
            "User name",
            "Graylog",
            "Set your bot's' user name in Slack",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_COLOR,
            "Color",
            "#FF0000",
            "Color to use for Slack message",
            ConfigurationField.Optional.NOT_OPTIONAL));
    configurationRequest.addField(
        new BooleanField(
            CK_ADD_STREAM_INFO,
            "Include stream information",
            false,
            "Include stream information in Slack attachment"));
    configurationRequest.addField(
        new NumberField(
            CK_ADD_BLITEMS,
            "Backlog items",
            1,
            "Number of backlog item descriptions to attach. If value is 0, no backlog will be included"));

    configurationRequest.addField(
        new BooleanField(
            CK_SHORT_MODE,
            "Short mode",
            true,
            "Enable short mode? This strips down the Slack message to the bare minimum to take less space in the chat room."));
    configurationRequest.addField(
        new TextField(
            CK_NOTIFY_USER,
            "Notify User",
            "",
            "Also notify user in channel by adding @user to the message. You can also use ${field[:-default]} in this text. If acknowledgement is enabled, you need to provide Slack's token too.",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new BooleanField(
            CK_LINK_NAMES,
            "Link names",
            true,
            "Find and create links for channel names and user names"));
    configurationRequest.addField(
        new TextField(
            CK_MESSAGE_ICON,
            "Message Icon",
            null,
            "Set a Slack emoji or an image URL to use as an icon",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_FOOTER_TEXT,
            "Footer Text",
            "${source}",
            "(For Notification) Add some brief text to help contextualize and identify an attachment. You can also use ${field[:-default]} in this text.",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_FOOTER_ICON_URL,
            "Footer Icon",
            null,
            "(For Notification) Set an image URL to use as a small icon beside your footer text",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_FOOTER_TS_FIELD,
            "Timestamp Field",
            "timestamp",
            "(For Notification) A timestamp field for displaying a timestamp value as part of the attachment's footer",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_GRAYLOG2_URL,
            "Graylog URL",
            null,
            "URL to your Graylog web interface. Used to build links in alarm notification.",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_PROXY_ADDRESS,
            "Proxy",
            null,
            "Please insert the proxy information in the following format: <ProxyAddress>:<Port>",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new TextField(
            CK_FIELDS,
            "Backlog fields",
            null,
            "Add fields from backlog item(s) into alert (field1, field2...).",
            ConfigurationField.Optional.OPTIONAL));
    configurationRequest.addField(
        new BooleanField(
            CK_ACKNOWLEDGE,
            "Add acknowledge button",
            false,
            "Include acknowledge buttons in alert message. This feature require either webhook URL from Slack app or Slack token. (Recommend Slack token)"));
    configurationRequest.addField(
        new BooleanField(
            CK_PREFORMAT,
            "Use pre-formatted text",
            false,
            "Create a block of pre-formatted, fixed-width text on backlog items"));
    // To use Slack Interactive Button, you need Slack App.
    // To mention user on Slack App's web hook, you need Slack Token
    configurationRequest.addField(
        new TextField(
            CK_TOKEN,
            "Slack Token",
            null,
            "Require if you want to use acknowledge buttons with user notification. Slack do not allow to mention user while using webhook URL from Slack app.",
            ConfigurationField.Optional.OPTIONAL));
    return configurationRequest;
  }

  protected static void checkConfiguration(Configuration configuration)
      throws ConfigurationException {
    if (!configuration.stringIsSet(CK_WEBHOOK_URL)) {
      throw new ConfigurationException(CK_WEBHOOK_URL + " is mandatory and must not be empty.");
    }

    if (!configuration.stringIsSet(CK_CHANNEL)) {
      throw new ConfigurationException(CK_CHANNEL + " is mandatory and must not be empty.");
    }

    if (!configuration.stringIsSet(CK_COLOR)) {
      throw new ConfigurationException(CK_COLOR + " is mandatory and must not be empty.");
    }

    checkUri(configuration, CK_PROXY_ADDRESS);
    checkUri(configuration, CK_GRAYLOG2_URL);
    checkUri(configuration, CK_FOOTER_ICON_URL);
  }

  public static boolean isValidUriScheme(URI uri, String... validSchemes) {
    return uri.getScheme() != null && Arrays.binarySearch(validSchemes, uri.getScheme(), null) >= 0;
  }

  private static void checkUri(Configuration configuration, String settingName)
      throws ConfigurationException {
    if (configuration.stringIsSet(settingName)) {
      try {
        final URI uri = new URI(configuration.getString(settingName));
        if (!isValidUriScheme(uri, "http", "https")) {
          throw new ConfigurationException(settingName + " must be a valid HTTP or HTTPS URL.");
        }
      } catch (URISyntaxException e) {
        throw new ConfigurationException("Couldn't parse " + settingName + " correctly.", e);
      }
    }
  }

  protected String buildStreamLink(String baseUrl, Stream stream) {
    StringBuilder builder = new StringBuilder(baseUrl);
    if (!baseUrl.endsWith("/")) {
        builder.append('/');
    }
    return builder.append("streams/").append(stream.getId()).append("/messages?q=*&rangetype=relative&relative=3600").toString();
  }

  protected String buildMessageLink(String baseUrl, Message message) {
    StringBuilder builder = new StringBuilder(baseUrl);
    if (!baseUrl.endsWith("/")) {
        builder.append('/');
    }
    return builder.append("messages/").append(message.getField("gl2_document_index")).append('/').append(message.getId()).toString();
  }
}
