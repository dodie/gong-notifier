package ch.adnovum.gong.notifier.email;

import ch.adnovum.gong.notifier.CachedPipelineInfoProvider;
import ch.adnovum.gong.notifier.DebugNotificationListener;
import ch.adnovum.gong.notifier.GongNotifierPluginBase;
import ch.adnovum.gong.notifier.PipelineInfoProvider;
import ch.adnovum.gong.notifier.go.api.GoServerApi;
import ch.adnovum.gong.notifier.util.GongUtil;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;

@Extension
public class GongNotifierEmailPlugin extends GongNotifierPluginBase {

	private static Logger LOGGER = Logger.getLoggerFor(GongNotifierEmailPlugin.class);
	private static final String PLUGIN_ID = "ch.adnovum.gong.notifier.email";

	private Gson gson = new Gson();
	private PipelineInfoProvider pipelineInfo;

	public GongNotifierEmailPlugin() {
		super(PLUGIN_ID,
				PluginSettings.class,
				PluginSettings.FIELD_CONFIG,
				GongUtil.readResourceString("/plugin-settings.template.html"));
	}

	@Override
	protected void reinit() {
		PluginSettings settings = (PluginSettings) getSettings();
		LOGGER.info("Re-initializing with settings: " + gson.toJson(settings).
				replaceAll("\"restPassword\":\"[^ \"]*\"","\"restPassword\":\"***\""));

		pipelineInfo = new CachedPipelineInfoProvider(
				new GoServerApi(settings.getServerUrl())
						.setAdminCredentials(settings.getRestUser(), settings.getRestPassword()));
		EmailSender sender = new JavaxEmailSender(settings.getSmtpHost(), settings.getSmtpPort());

		addListener(new DebugNotificationListener());
		addListener(new EmailNotificationListener(pipelineInfo, sender, settings));
	}

	@Override
	protected PipelineInfoProvider getPipelineInfoProvider() {
		return pipelineInfo;
	}
}
