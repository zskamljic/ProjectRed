package mrtjp.projectred.illumination.data;

import mrtjp.projectred.illumination.BlockLightType;
import mrtjp.projectred.illumination.MultipartLightType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static mrtjp.projectred.illumination.ProjectRedIllumination.MOD_ID;
import static mrtjp.projectred.illumination.init.IlluminationBlocks.ILLUMAR_SMART_LAMP;

public class IlluminationLanguageProvider extends LanguageProvider {

    private static final String[] LOCAL_COLORS = new String[] {
            "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
    };

    public IlluminationLanguageProvider(PackOutput output) {
        super(output, MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        // Creative tab
        add("itemGroup." + MOD_ID, "Project Red: Illumination");

        // Block lights
        for (BlockLightType type : BlockLightType.values()) {
            for (int color = 0; color < 16; color++) {
                add(type.getBlock(color, false), createLocalizedLightName(color, false, type.getLocalBaseName()));
                add(type.getBlock(color, true), createLocalizedLightName(color, true, type.getLocalBaseName()));
            }
        }

        // Illumar smart lamp
        addBlock(ILLUMAR_SMART_LAMP, "Illumar Smart Lamp");

        // Multipart lights
        for (MultipartLightType type : MultipartLightType.values()) {
            for (int color = 0; color < 16; color++) {
                add(type.getItem(color, false), createLocalizedLightName(color, false, type.getLocalBaseName()));
                add(type.getItem(color, true), createLocalizedLightName(color, true, type.getLocalBaseName()));
            }
        }
    }

    public static String createLocalizedLightName(int color, boolean inverted, String lightName) {
        return LOCAL_COLORS[color] + (inverted ? " Inverted " : " ") + lightName;
    }
}
