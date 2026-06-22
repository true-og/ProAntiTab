package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.sender.CommandSender;

public class BlockSlashCommandsSection extends ConfigStorage {

    public boolean ENABLED;

    public BlockSlashCommandsSection() {

        super("block-slash-commands");

    }

    @Override
    public void load() {

        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", false).getOrSet();

    }

    public boolean isCommand(String command) {

        if (!ENABLED) {

            return false;

        }

        return StringUtils.getFirstArg(command).contains("/");

    }

    public boolean doesBypass(CommandSender sender) {

        return !ENABLED || PermissionUtil.hasPermission(sender, "slash");

    }

}
