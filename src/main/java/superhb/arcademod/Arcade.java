package superhb.arcademod;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import superhb.arcademod.client.UpdateAnnouncer;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.gui.GuiPrize;
import superhb.arcademod.proxy.CommonProxy;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.*;
import superhb.arcademod.tileentity.TileEntityArcade;
import superhb.arcademod.tileentity.TileEntityPlushie;
import superhb.arcademod.tileentity.TileEntityPrize;
import superhb.arcademod.util.EnumType;
import superhb.arcademod.util.PrizeList;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
    "1.1.0": "Created all Tetromino shapes",
    "1.1.1": "Added random picker for next shape to Tetromino",
    "1.2.0": "Created Prize Block and GUI"
 */
@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, updateJSON = Reference.UPDATE_URL)
public class Arcade {
    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.SERVER_PROXY)
    public static CommonProxy proxy;

    @Instance(Reference.MODID)
    public static Arcade instance;

    // Logger
    public static final Logger logger = LogManager.getLogger(Reference.MODID);

    // Creative Tab
    public static final CreativeTabs tab = new CreativeTabs(Reference.MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ArcadeItems.coin);
        }

        @Override
        public String getTranslatedTabLabel () {
            return I18n.format("mod.arcademod:name.locale");
        }
    };

    public static Set<Map.Entry<ComparableVersion, String>> changelog;
    public static ForgeVersion.Status status;

    // Configuration Variables
    public static boolean disableCoins;
    public static boolean requireRedstone;
    public static boolean disableUpdateNotification;

    public static int prizeTotal = 0;
    public static PrizeList[] prizeList;

    // TODO: default add all plushies
    private final String[] defaultList  = {
        "arcademod:plushie:5:Mob=0"
    };

    // Game Addons
    public static File gameDir;

    @EventHandler
    public void preInit (FMLPreInitializationEvent event) {
        // Mod info
        event.getModMetadata().autogenerated = false;
        event.getModMetadata().credits = Reference.CREDIT;
        event.getModMetadata().authorList.add(Reference.AUTHOR);
        event.getModMetadata().description = Reference.DESCRIPTION;
        event.getModMetadata().url = Reference.URL;
        event.getModMetadata().logoFile = Reference.LOGO;
        event.getModMetadata().updateJSON = Reference.UPDATE_URL;

        // Configuration File
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        disableCoins = config.getBoolean("disableCoins", Configuration.CATEGORY_GENERAL, false, "Disable the need to use coins to play the arcade machines");
        requireRedstone = config.getBoolean("requireRedstone", Configuration.CATEGORY_GENERAL, false, "Require the machines to be powered by redstone to play");
        disableUpdateNotification = config.getBoolean("disableUpdateNotification", Configuration.CATEGORY_GENERAL, false, "Disable message in chat when update is available");
        prizeTotal = config.getInt("total", "prize_list", 1, 0, 100, "Amount of prizes in list. This has to be changed manually.");
        prizeList = new PrizeList[prizeTotal];
        for (int i = 0; i < prizeTotal; i++) {
            String temp = config.getString(String.format("%d", i), "prize_list", defaultList[i], "Format: name:cost");
            String[] s = temp.split(":");
            logger.info(String.format("Prize Format has [%d]", s.length));
            if (s.length == 3) {
                Item item = Item.getByNameOrId(s[0] + ":" + s[1]);
                int cost = new Integer(s[2]);
                prizeList[i] = new PrizeList(new ItemStack(item), cost);
            } else if (s.length == 4) { // TODO: Figure out NBT
                Item item = Item.getByNameOrId(s[0] + ":" + s[1]);
                int cost = new Integer(s[2]);
                String[] nbt = s[3].split("=");
                ItemStack stack = new ItemStack(item);

                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setInteger(nbt[0], new Integer(nbt[1]));
                prizeList[i] = new PrizeList(stack, cost);
            }
        }
        config.save();

        // Game Addons
        gameDir = new File(event.getModConfigurationDirectory().getParent(), "/Arcade_Games/");
        if (!gameDir.exists()) {
            logger.info("Games Addon directory doesn't exist. Creating empty folder...");
            gameDir.mkdir();
            gameDir.mkdirs();
        }

        // Register TileEntity
        GameRegistry.registerTileEntity(TileEntityArcade.class, Reference.MODID + ":tile_arcade");
        GameRegistry.registerTileEntity(TileEntityPlushie.class, Reference.MODID + ":tile_plushie");
        GameRegistry.registerTileEntity(TileEntityPrize.class, Reference.MODID + ":tile_prize");

        // Register Event
        if (!disableUpdateNotification) MinecraftForge.EVENT_BUS.register(new UpdateAnnouncer());

        proxy.preInit(event);
    }

    @EventHandler
    public void init (FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit (FMLPostInitializationEvent event) {
        for (ModContainer mod : Loader.instance().getModList()) {
            if (mod.getModId().equals(Reference.MODID)) {
                status = ForgeVersion.getResult(mod).status;
                if (status == ForgeVersion.Status.OUTDATED || status == ForgeVersion.Status.BETA_OUTDATED) changelog = ForgeVersion.getResult(mod).changes.entrySet();
            }
        }

        // Check for other mods here
        // TODO: Move creation of prizeList here

        proxy.postInit(event);
    }

    // TODO: Game Addons
    // Gets files from /Arcade_Games/ directory
    private void getTypeFiles (List<File> games) {
        for (File game : games) {
            if (game.isDirectory()) {
                for (EnumType type : EnumType.values()) {

                }
            }
        }
    }
}
