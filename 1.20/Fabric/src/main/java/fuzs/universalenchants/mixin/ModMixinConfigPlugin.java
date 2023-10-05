package fuzs.universalenchants.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.universalenchants.CasualStreamHandler;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_NAME = "dynamic/EnchantmentMixin";
    private static final String[] CLASS_TARGETS = {
            "net/minecraft/world/item/enchantment/Enchantment",
            "net/minecraft/world/item/enchantment/ProtectionEnchantment",
            "net/minecraft/world/item/enchantment/OxygenEnchantment",
            "net/minecraft/world/item/enchantment/WaterWorkerEnchantment",
            "net/minecraft/world/item/enchantment/ThornsEnchantment",
            "net/minecraft/world/item/enchantment/WaterWalkerEnchantment",
            "net/minecraft/world/item/enchantment/FrostWalkerEnchantment",
            "net/minecraft/world/item/enchantment/BindingCurseEnchantment",
            "net/minecraft/world/item/enchantment/SoulSpeedEnchantment",
            "net/minecraft/world/item/enchantment/SwiftSneakEnchantment",
            "net/minecraft/world/item/enchantment/DamageEnchantment",
            "net/minecraft/world/item/enchantment/KnockbackEnchantment",
            "net/minecraft/world/item/enchantment/FireAspectEnchantment",
            "net/minecraft/world/item/enchantment/LootBonusEnchantment",
            "net/minecraft/world/item/enchantment/SweepingEdgeEnchantment",
            "net/minecraft/world/item/enchantment/DiggingEnchantment",
            "net/minecraft/world/item/enchantment/UntouchingEnchantment",
            "net/minecraft/world/item/enchantment/DigDurabilityEnchantment",
            "net/minecraft/world/item/enchantment/ArrowDamageEnchantment",
            "net/minecraft/world/item/enchantment/ArrowKnockbackEnchantment",
            "net/minecraft/world/item/enchantment/ArrowFireEnchantment",
            "net/minecraft/world/item/enchantment/ArrowInfiniteEnchantment",
            "net/minecraft/world/item/enchantment/FishingSpeedEnchantment",
            "net/minecraft/world/item/enchantment/TridentLoyaltyEnchantment",
            "net/minecraft/world/item/enchantment/TridentImpalerEnchantment",
            "net/minecraft/world/item/enchantment/TridentRiptideEnchantment",
            "net/minecraft/world/item/enchantment/TridentChannelingEnchantment",
            "net/minecraft/world/item/enchantment/MultiShotEnchantment",
            "net/minecraft/world/item/enchantment/QuickChargeEnchantment",
            "net/minecraft/world/item/enchantment/ArrowPiercingEnchantment",
            "net/minecraft/world/item/enchantment/MendingEnchantment",
            "net/minecraft/world/item/enchantment/VanishingCurseEnchantment"
    };

    private final Map<String, byte[]> classGenerators = Maps.newHashMap();
    private final List<String> mixins = Lists.newArrayList();


    @Override
    public void onLoad(String rawMixinPackage) {
        String mixinPackage = rawMixinPackage.replace('.', '/');

        InputStream inputStream = this.getClass().getResourceAsStream(MIXIN_NAME + ".class");
        Objects.requireNonNull(inputStream, "input stream is null");
        try {
            ClassReader classReader = new ClassReader(inputStream);
            inputStream.close();

            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            classNode.name = MIXIN_NAME + "2";
            classNode.invisibleAnnotations.removeIf(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"));
            AnnotationVisitor mixinAnnotation = classNode.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
            AnnotationVisitor targetAnnotation = mixinAnnotation.visitArray("value");
            for (String target : CLASS_TARGETS) targetAnnotation.visit(null, Type.getType('L' + target + ';'));
            targetAnnotation.visitEnd();
            mixinAnnotation.visitEnd();

            ClassWriter classWriter = new ClassWriter(0);
            classNode.accept(classWriter);
            byte[] byteArray = classWriter.toByteArray();

            classGenerators.put('/' + mixinPackage + "/" + MIXIN_NAME + "2.class", byteArray);
            mixins.add(MIXIN_NAME.replace('/', '.') + "2");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        this.generate(mixinPackage, "dynamic/EnchantmentMixin2", CLASS_TARGETS);

//        addReplacers(mixinPackage);
//        fishAddURL().accept(CasualStreamHandler.create(classGenerators));
        try {
            fishAddURL().accept(new URL("magic", null, -1, "/", new URLStreamHandler() {

                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    if (!classGenerators.containsKey(url.getPath())) return null;
                    return new URLConnection(url) {

                        @Override
                        public void connect() throws IOException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return new ByteArrayInputStream(classGenerators.get(url.getPath()));
                        }

                        @Override
                        public Permission getPermission() throws IOException {
                            return null;
                        }
                    };
                }
            }));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addReplacers(String mixinPackage) {
        Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if (transformer == null) throw new IllegalStateException("Not running with a transformer?");

        Extensions extensions = null;
        try {
            for (Field f : transformer.getClass().getDeclaredFields()) {
                if (f.getType() == Extensions.class) {
                    f.setAccessible(true); //Knock knock, we need this
                    extensions = (Extensions) f.get(transformer);
                    break;
                }
            }

            if (extensions == null) {
                String foundFields = Arrays.stream(transformer.getClass().getDeclaredFields()).map(f -> f.getType() + " " + f.getName()).collect(Collectors.joining(", "));
                throw new NoSuchFieldError("Unable to find extensions field, only found " + foundFields);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Running with a transformer that doesn't have extensions?", e);
        }

//        extensions.add(new Target.Extension(mixinPackage, classReplacers));
        ExtensionClassExporter exporter = extensions.getExtension(ExtensionClassExporter.class);
        CasualStreamHandler.dumper = (name, bytes) -> {
            ClassNode node = new ClassNode(); //Read the bytes in as per TreeTransformer#readClass(byte[])
            new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
            exporter.export(MixinEnvironment.getCurrentEnvironment(), name, false, node);
        };
    }

    private void generate(String mixinPackage, String name, String... targets) {
        //System.out.println("Generating " + mixinPackage + name + " with targets " + targets);
        classGenerators.put('/' + mixinPackage + "/" + name + ".class", makeMixinBlob(mixinPackage + "/" + name, targets));
        //ClassTinkerers.define(mixinPackage + name, makeMixinBlob(mixinPackage + name, targets)); ^^^
        mixins.add(name.replace('/', '.'));
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FabricLoader.getInstance().isModLoaded("puzzleslib");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!targetClassName.contains("Enchantment")) return;
        for (MethodNode method : targetClass.methods) {
            if (method.name.equals("getMaxLevel")) return;
        }
        System.out.println(targetClassName);
//        ClassNode classNode = new ClassNode();
//        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//        targetClass.accept(classWriter);
//        writeGetMaxLevel(targetClass);
//        classNode.visitMethod()
//        targetClass.methods.addAll(classNode.methods);
        System.out.println();
    }

    private static Consumer<URL> fishAddURL() {
        ClassLoader loader = ModMixinConfigPlugin.class.getClassLoader();
        Method addUrlMethod = null;
        for (Method method : loader.getClass().getDeclaredMethods()) {
			/*System.out.println("Type: " + method.getReturnType());
			System.out.println("Params: " + method.getParameterCount() + ", " + Arrays.toString(method.getParameterTypes()));*/
            if (method.getReturnType() == Void.TYPE && method.getParameterCount() == 1 && method.getParameterTypes()[0] == URL.class) {
                addUrlMethod = method; //Probably
                break;
            }
        }
        if (addUrlMethod == null) throw new IllegalStateException("Couldn't find method in " + loader);
        try {
            addUrlMethod.setAccessible(true);
            MethodHandle handle = MethodHandles.lookup().unreflect(addUrlMethod);
            return url -> {
                try {
                    handle.invoke(loader, url);
                } catch (Throwable t) {
                    throw new RuntimeException("Unexpected error adding URL", t);
                }
            };
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Couldn't get handle for " + addUrlMethod, e);
        }
    }

    static byte[] makeMixinBlob(String name, String... targets) {
        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(52, ACC_PUBLIC | ACC_ABSTRACT | Opcodes.ACC_INTERFACE, name, null, "java/lang/Object", null);

        AnnotationVisitor mixinAnnotation = classWriter.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor targetAnnotation = mixinAnnotation.visitArray("value");
        for (String target : targets) targetAnnotation.visit(null, Type.getType('L' + target + ';'));
        targetAnnotation.visitEnd();
        mixinAnnotation.visitEnd();

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    static void writeGetMaxLevel(ClassVisitor classVisitor) {
        MethodVisitor methodVisitor = classVisitor.visitMethod(ACC_PUBLIC, "getMaxLevel", "()I", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
//        methodVisitor.visitLineNumber(16, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "fuzs/universalenchants/world/item/enchantment/data/MaxLevelManager", "getMaxLevel", "(Lnet/minecraft/world/item/enchantment/Enchantment;)Ljava/lang/Integer;", false);
        methodVisitor.visitVarInsn(ASTORE, 1);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
//        methodVisitor.visitLineNumber(17, label1);
        methodVisitor.visitVarInsn(ALOAD, 1);
        Label label2 = new Label();
        methodVisitor.visitJumpInsn(IFNULL, label2);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitLabel(label2);
//        methodVisitor.visitLineNumber(18, label2);
        methodVisitor.visitFrame(F_APPEND, 1, new Object[]{"java/lang/Integer"}, 0, null);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "net/minecraft/world/item/enchantment/Enchantment", "getMaxLevel", "()I", false);
        methodVisitor.visitInsn(IRETURN);
//        Label label3 = new Label();
//        methodVisitor.visitLabel(label3);
//        methodVisitor.visitLocalVariable("this", "Lfuzs/universalenchants/ModEnchantment;", null, label0, label3, 0);
//        methodVisitor.visitLocalVariable("maxLevel", "Ljava/lang/Integer;", null, label1, label3, 1);
//        methodVisitor.visitMaxs(1, 2);
        methodVisitor.visitEnd();
    }
}
