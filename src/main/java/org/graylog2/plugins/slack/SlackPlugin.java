package org.graylog2.plugins.slack;

import java.util.Collection;
import java.util.Collections;
import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

public class SlackPlugin implements Plugin {
  @Override
  public Collection<PluginModule> modules() {
    return Collections.<PluginModule>singleton(new SlackPluginModule());
  }

  @Override
  public PluginMetaData metadata() {
    return new SlackPluginMetadata();
  }
}
