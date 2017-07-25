Slack/Mattermost Plugin for Graylog
========================

[![Build Status](https://travis-ci.org/Graylog2/graylog-plugin-slack.svg)](https://travis-ci.org/Graylog2/graylog-plugin-slack)

**Required Graylog version:** 2.0 and later.

Please use version 2.1.0 of this plugin if you are still running Graylog 1.x


## Features

### Message output
Forward messages on streams via message output to Slack.

The screenshot below shows a sample of message output Slack .

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshort_message.png)

### Notification
Send notification messages to Slack when alert was raised. 

The screenshot below shows a sample of Slack notification.

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshort_alert.png)

* Send message directly to user or channel
* Support Slack attachment short mode
* Mention users or channels when alert. Users can be mentioned by field variables.
* Provide link back to event times
* Support event timestamp in footer text
* Support proxy
* Support custom fields in Slack attachment
* Support acknowledge buttons. Required Slack app's token
* Support pre-formatted text in backlog item

The screenshot below shows an acknowledgement buttons

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshort_acknowledgement.png)

The screenshot below shows a result of acknowledged

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshort_acknowledged.png)

The screenshot below shows a pre-formatted text with acknowledgement buttons

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshot_preformat.png)


## Upgrade from Graylog plugin Slack version 1.4
1. You can [Download the plugin](https://github.com/Graylog2/graylog-plugin-slack/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

2. Remove all previous version of Graylog plugin Slack `.jar` files. in `plugins` directory.

3. Restart a graylog server. Plugin will automatically migrate all your configured data to a new version.

## Usage

### For Slack:

#### Step 1: Create Slack Incoming Webhook

Create a new Slack Incoming Webhook (`https://<organization>.slack.com/services/new/incoming-webhook`) and copy the URL it will present to you. It will ask you to select a Slack channel but you can override it in the plugin configuration later.

### For Mattermost:

#### Step 1: Create Mattermost Incoming Webhook

Enable Webhooks in general and create an incoming Webhook for Graylog as described in the [Mattermost docs](http://docs.mattermost.com/developer/webhooks-incoming.html).

This plugin is not tested on Mattermost. It should provide a compatibility as of version 1.4.

#### Step 2: Create alarm callback or message output

Create a "Slack Alarm Callback" on the "Manage notifications" page of your alert. Or "Slack Output" on "Manage outputs" page of your stream. Enter the requested configuration (use the Incoming Webhook URL you created in step 1) and save. Make sure you also configured alert conditions for the stream so that the alerts are actually triggered.

### Configuration attributes

* Webhook URL - An URL for sending message to Slack. This field is not required when Slack's token is used.
* Color - A color of Slack attachment. I recommend `#FF0000` (red) color for alert and `#0000FF` (blue) color for output
* Channel - A Slack channel or user name to receive a message.
* Message icon - Override Slack bot icon with this icon URL. You can leave this field empty to use icon which is configured when creating Slack webhook or Slack app.
* User name - Override Slack bot user name. You can leave this field empty to use a bot name which is configured when creating Slack webhook or Slack app.
* Include stream information - You can extend a message by including Graylog Stream information
* Notify user - When message is sent to Slack, also tag these users or channels. You can directly enter user name in `@user` or use a field variable to tag a user name which was found on a log. For example, `${login_by}` will tag a Slack user which name was found in Graylog log's field `login_by`
* Footer icon - Add a footer icon to Slack message
* Timestamp field - Add a Slack message timestamp. By default, a timestamp should be selected from `timestamp` field
* Graylog URL - If this field provided, it will add a link back to Graylog server on Slack message
* Add acknowledge button - Display acknowledge buttons on Slack message. This will allow Slack user to interact with a message. This function requires Slack token.
* Slack token - A Slack token which is generated from Slack app. This token is required to interact with acknowledge buttons. If Slack `token` is provided,  `Webhook URL` can be omitted

The same applies for message outputs which you can configure in *Stream* - > *Manage Outputs*.

## Troubleshooting

### HTTPS connection fails

If the Java runtime environment and the included SSL certificate trust store is too old, HTTPS connections to Slack might fail with the following error message:

```text
Caused by: javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```

In this case, add the Slack SSL certificate manually to Java's trust store similar to the process described in the [Graylog documentation](http://docs.graylog.org/en/2.1/pages/configuration/https.html#adding-a-self-signed-certificate-to-the-jvm-trust-store).

### Error occurred when clicks a Slack button.
Slack app required HTTPS connection between your Graylog server and Slack server. You need to configure your Graylog server with a valid certificate file. Make sure your Graylog server open a firewall allow Slack server to communicate with. 

Most common errors and describe will be send to you by Slack's bot when you click a button. If trouble persists, you can turn a `debug` log on Graylog server to see more detail on log files. 

Please see more information on Slack aps here https://api.slack.com/slack-apps

## Build

This project is using Maven and requires Java 8 or higher.

You can build a plugin (JAR) with `mvn package`.

DEB and RPM packages can be build with `mvn jdeb:jdeb` and `mvn rpm:rpm` respectively.

## Plugin Release

We are using the maven release plugin:

```
$ mvn release:prepare
[...]
$ mvn release:perform
```

This sets the version numbers, creates a tag and pushes to GitHub. TravisCI will build the release artifacts and upload to GitHub automatically.
