Slack Plugin for Graylog
========================

**Note:** This plugin has been rewriten to support Graylog 3.1. If you're using Graylog 3.1 and later, please checkout https://github.com/omise/graylog-plugin-slack-notification

**Note:** This plugin is forked from https://github.com/graylog-labs/graylog-plugin-slack.
The features are submitted as pull-request to original Graylog Plugin and still waiting for reviewing.

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

The screenshot below shows a pre-formatted text with acknowledgement buttons

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshot_preformat.png)

#### Mention users or channels when alert
This feature requires Slack Token. Slack API does not allow a webhook to mention users. To setup a Slack App, please see https://api.slack.com/slack-apps

#### Acknowledgment buttons
The acknowledgment buttons also requires Slack Token. You cannot use Slack Incoming Webhook to creates buttons. See [Slack Interactive Message](https://api.slack.com/interactive-messages) for detail of Slack API.


The screenshot below shows an acknowledgement buttons

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshort_acknowledgement.png)

The screenshot below shows a result of acknowledged

![](https://github.com/omise/graylog-plugin-slack/blob/omise/screenshort_acknowledged.png)


## Installation 
1. You can [Download the plugin](https://github.com/omise/graylog-plugin-slack/releases) and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default and can be configured in your `graylog.conf` file.

2. Remove all previous version of Graylog plugin Slack `.jar` files. in `plugins` directory.

3. Restart a graylog server. Plugin will automatically migrate all your configured data to a new version.

## Usage

### For Slack:

#### Step 1: Create Slack Incoming Webhook

Create a new Slack Incoming Webhook (`https://<organization>.slack.com/services/new/incoming-webhook`) and copy the URL it will present to you. It will ask you to select a Slack channel but you can override it in the plugin configuration later.

#### Step 2: Create Slack App (If you want to mention someone when send notifications to Slack or use interactive buttons)
Create a new Slack App https://api.slack.com/apps?new_app=1 and copy the Slack Token into plugin configuration.

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
