package org.lnicholls.galleon.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.lnicholls.galleon.server.MusicPlayerConfiguration;
import org.lnicholls.galleon.server.*;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;

import com.tivo.hme.bananas.BText;
import com.tivo.hme.sdk.Resource;

public class MusicOptionsScreen extends DefaultOptionsScreen {

	public MusicOptionsScreen(DefaultApplication app, Resource background) {
		super(app);

		mBackground = background;

		getBelow().setResource(mBackground);

		MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getServerConfiguration()
				.getMusicPlayerConfiguration();

		int start = TOP;
		int width = 280;
		int increment = 37;
		int height = 25;
		BText text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Player");

		NameValue[] nameValues = new NameValue[] {
				new NameValue(StringUtils.capitalize(MusicPlayerConfiguration.CLASSIC),
						MusicPlayerConfiguration.CLASSIC),
				new NameValue(StringUtils.capitalize(MusicPlayerConfiguration.WINAMP), MusicPlayerConfiguration.WINAMP) };
		mPlayerButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height, true,
				nameValues, musicPlayerConfiguration.getPlayer());
		setFocusDefault(mPlayerButton);

		start = start + increment;

		text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Winamp Classic Skin");

		List skins = Server.getServer().getWinampSkins();
		Iterator iterator = skins.iterator();
		List nameValuesList = new ArrayList();
		while (iterator.hasNext()) {
			File file = (File) iterator.next();
			try {
				String name = Tools.extractName(file.getCanonicalPath());
				nameValuesList.add(new NameValue(name, file.getCanonicalPath()));
			} catch (Exception ex) {
			}
		}
		nameValues = (NameValue[]) nameValuesList.toArray(new NameValue[0]);
		mSkinButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height, true,
				nameValues, musicPlayerConfiguration.getSkin());

		start = start + increment;

		text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Random Play Folders");
		nameValues = new NameValue[] { new NameValue("Yes", "true"), new NameValue("No", "false") };
		mRandomPlayButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height,
				true, nameValues, String.valueOf(musicPlayerConfiguration.isRandomPlayFolders()));

		start = start + increment;

		text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Screensaver");
		mScreensaverButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height,
				true, nameValues, String.valueOf(musicPlayerConfiguration.isScreensaver()));

		start = start + increment;
		
		ServerConfiguration serverConfiguration = Server.getServer().getServerConfiguration();
		
		text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Disable Timeout");
		mDisableTimeoutButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height, true,
				nameValues, String.valueOf(serverConfiguration.isDisableTimeout()));

		/*
		text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Use Amazon.com");
		mUseAmazonButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height, true,
				nameValues, String.valueOf(musicPlayerConfiguration.isUseAmazon()));

		start = start + increment;

		text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Use folder.jpg");
		mUseFolderButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height, true,
				nameValues, String.valueOf(musicPlayerConfiguration.isUseFile()));

		*/
		
		start = start + increment;

		text = new BText(getNormal(), BORDER_LEFT, start, BODY_WIDTH, 30);
		text.setFlags(RSRC_HALIGN_LEFT | RSRC_TEXT_WRAP | RSRC_VALIGN_CENTER);
		text.setFont("default-24-bold.font");
		text.setShadow(true);
		text.setValue("Show web images");
		mShowWebImagesButton = new OptionsButton(getNormal(), BORDER_LEFT + BODY_WIDTH - width, start, width, height,
				true, nameValues, String.valueOf(musicPlayerConfiguration.isShowImages()));
	}

	public boolean handleEnter(java.lang.Object arg, boolean isReturn) {
		getBelow().setResource(mBackground);

		return super.handleEnter(arg, isReturn);
	}

	public boolean handleExit() {

		try {
			DefaultApplication application = (DefaultApplication)getApp();
			if (!application.isDemoMode())
			{
				MusicPlayerConfiguration musicPlayerConfiguration = Server.getServer().getServerConfiguration()
						.getMusicPlayerConfiguration();
				musicPlayerConfiguration.setPlayer(mPlayerButton.getValue());
				musicPlayerConfiguration.setSkin(mSkinButton.getValue());
				//musicPlayerConfiguration.setUseAmazon(Boolean.valueOf(mUseAmazonButton.getValue()).booleanValue());
				//musicPlayerConfiguration.setUseFile(Boolean.valueOf(mUseFolderButton.getValue()).booleanValue());
				musicPlayerConfiguration.setShowImages(Boolean.valueOf(mShowWebImagesButton.getValue()).booleanValue());
				musicPlayerConfiguration.setRandomPlayFolders(Boolean.valueOf(mRandomPlayButton.getValue()).booleanValue());
				musicPlayerConfiguration.setScreensaver(Boolean.valueOf(mScreensaverButton.getValue()).booleanValue());
	
				Server.getServer().updateMusicPlayerConfiguration(musicPlayerConfiguration);
				
				ServerConfiguration serverConfiguration = Server.getServer().getServerConfiguration();
				Server.getServer().setDisableTimeout(Boolean.valueOf(mDisableTimeoutButton.getValue()).booleanValue());
			}
		} catch (Exception ex) {
			Tools.logException(MusicOptionsScreen.class, ex, "Could not configure music player");
		}
		return super.handleExit();
	}

	private OptionsButton mPlayerButton;

	private OptionsButton mSkinButton;

	private OptionsButton mRandomPlayButton;

	private OptionsButton mScreensaverButton;
	
	private OptionsButton mDisableTimeoutButton;

	//private OptionsButton mUseAmazonButton;

	//private OptionsButton mUseFolderButton;

	private OptionsButton mShowWebImagesButton;

	private Resource mBackground;
}