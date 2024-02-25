package fuzs.universalenchants;

import com.chocohead.mm.api.ClassTinkerers;
import com.google.gson.Gson;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import org.objectweb.asm.AnnotationVisitor;

import java.io.FileReader;
import java.nio.file.Path;

public class EarlyRiserEntryPoint implements Runnable {

    @Override
    public void run() {


        String[] targets;
        Path path = ModLoaderEnvironment.INSTANCE.getConfigDirectory().resolve(".universalenchantscache");
        try (FileReader fileReader = new FileReader(path.toFile())) {
            targets = new Gson().fromJson(fileReader, String[].class);
        } catch (Throwable throwable) {
            return;
        }

        if (targets.length == 0) {
            return;
        }


        ClassTinkerers.addReplacement("fuzs.universalenchants.mixin.inject.EnchantmentMixin", classNode -> {

            classNode.invisibleAnnotations.removeIf(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"));
            AnnotationVisitor mixinAnnotation = classNode.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
            AnnotationVisitor targetAnnotation = mixinAnnotation.visitArray("targets");
            for (String target : targets) {
                targetAnnotation.visit(null, target);
            }
            targetAnnotation.visitEnd();
            mixinAnnotation.visitEnd();

        });
    }
}
