package fuzs.universalenchants.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import cpw.mods.cl.ModuleClassLoader;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import it.unimi.dsi.fastutil.Pair;
import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.tools.obfuscation.interfaces.IReferenceManager;
import sun.misc.Unsafe;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.file.Path;
import java.security.Permission;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {
    private static final List<Pair<String, Path>> DYNAMIC_MIXINS = Lists.newArrayList();

    static {
        register("EnchantmentMixin", ".universalenchantscache");
    }

    private static void register(String mixinClassName, String targetsFile) {
        register(mixinClassName, ModLoaderEnvironment.INSTANCE.getConfigDirectory().resolve(targetsFile));
    }

    private static void register(String mixinClassName, Path targetsFile) {
        DYNAMIC_MIXINS.add(Pair.of("injected/" + mixinClassName.replace('.', '/'), targetsFile));
    }


    @Override
    public void onLoad(String mixinPackage) {

        mixinPackage = mixinPackage.replace('.', '/');
        Map<String, byte[]> classGenerators = Maps.newHashMap();

        Iterator<Pair<String, Path>> iterator = DYNAMIC_MIXINS.iterator();
        while (iterator.hasNext()) {

            Pair<String, Path> pair = iterator.next();

            String mixinClassName = pair.left();
            if (mixinClassName.startsWith(mixinPackage)) {
                mixinClassName = mixinClassName.substring(mixinPackage.length());
            }

            String[] targets;
            Path path = pair.right();
            try (FileReader fileReader = new FileReader(path.toFile())) {
                targets = new Gson().fromJson(fileReader, String[].class);
            } catch (Throwable throwable) {
                targets = null;
            }

            if (targets == null || targets.length == 0) {
                iterator.remove();
                continue;
            }

            InputStream inputStream = this.getClass().getResourceAsStream(mixinClassName + ".class");
            Objects.requireNonNull(inputStream, "input stream is null");
            try {

                ClassReader classReader = new ClassReader(inputStream);
                inputStream.close();

                ClassNode classNode = new ClassNode();
                classReader.accept(classNode, 0);

                classNode.invisibleAnnotations.removeIf(annotationNode -> annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;"));
                AnnotationVisitor mixinAnnotation = classNode.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
                AnnotationVisitor targetAnnotation = mixinAnnotation.visitArray("targets");
                for (String target : targets) {
                    targetAnnotation.visit(null, target);
                }
                targetAnnotation.visitEnd();
                mixinAnnotation.visitEnd();

                ClassWriter classWriter = new ClassWriter(0);
                classNode.accept(classWriter);
                byte[] byteArray = classWriter.toByteArray();

                classGenerators.put('/' + mixinPackage + "/$/" + mixinClassName + ".class", byteArray);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            getURLConsumer(this.getClass().getClassLoader()).accept(new URL("magic-at", null, -1, "/", new URLStreamHandler() {

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
                            return new ByteArrayInputStream(classGenerators.get(this.url.getPath()));
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

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FMLLoader.getLoadingModList().getModFileById("puzzleslib") != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return DYNAMIC_MIXINS.stream().map(Pair::left).map(t -> "$." + t.replace("/", ".")).collect(Collectors.toList());
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        mixinInfo.getConfig();
        Class<?> clazz = null;
        try {
            clazz = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Method method = null;
        for (Method cmethod : clazz.getMethods()) {
            if (cmethod.getReturnType() == IReferenceMapper.class && cmethod.getParameterTypes().length == 0) {
                method = cmethod;
                break;
            }
        }
        if (method != null) {
            method.setAccessible(true);
            try {
                IReferenceMapper referenceMapper = (IReferenceMapper) MethodHandles.publicLookup().unreflect(method).invoke(mixinInfo.getConfig());
                System.out.println(referenceMapper);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private static Consumer<URL> getURLConsumer(ClassLoader classLoader) {
        Class<?> clazz = ModuleClassLoader.class;
        if (!(classLoader instanceof ModuleClassLoader)) throw new IllegalStateException("Class loader not supported " + classLoader);
        if (true)
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);
            Field packageLookupField = clazz.getDeclaredField("packageLookup");
            long packageLookupOffset = unsafe.objectFieldOffset(packageLookupField);
            Map<String, ResolvedModule> packageLookup = (Map<String, ResolvedModule>) unsafe.getObject(classLoader, packageLookupOffset);
            Field parentLoadersField = clazz.getDeclaredField("parentLoaders");
            Type genericType = parentLoadersField.getGenericType();
            Class<?> type = parentLoadersField.getType();
            long parentLoadersOffset = unsafe.objectFieldOffset(parentLoadersField);
            Map<String, ClassLoader> parentLoaders = (Map<String, ClassLoader>) unsafe.getObject(classLoader, parentLoadersOffset);
            return url -> {
                String s = "fuzs.universalenchants.mixin.$.injected";
                ClassLoader remove = parentLoaders.remove(s);
//                packageLookup.remove(s);
                if (remove != null) {
                    remove = new DynamicURLClassLoader(new URL[]{url}, remove);
                } else {
                    remove = new DynamicURLClassLoader(new URL[]{url});
                }
                parentLoaders.put(s, remove);
            };
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        Method foundMethod = null;
        Class<? extends ClassLoader> aClass = classLoader.getClass();
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getReturnType() == void.class) {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == URL.class) {
                    foundMethod = method;
                    break;
                }
            }
        }
        if (foundMethod == null) {
            throw new IllegalStateException("Couldn't find method in " + classLoader);
        } else {
            try {
                foundMethod.setAccessible(true);
                MethodHandle handle = MethodHandles.lookup().unreflect(foundMethod);
                return url -> {
                    try {
                        handle.invoke(classLoader, url);
                    } catch (Throwable t) {
                        throw new RuntimeException("Unexpected error adding URL", t);
                    }
                };
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't get handle for " + foundMethod, e);
            }
        }
    }

    private static final class DynamicURLClassLoader extends URLClassLoader {
        private DynamicURLClassLoader(URL[] urls, ClassLoader classLoader) {
            super(urls, classLoader);
        }
        private DynamicURLClassLoader(URL[] urls) {
            super(urls, new DummyClassLoader());
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return super.getResourceAsStream(name);
        }

        static {
            registerAsParallelCapable();
        }
    }

    static class DummyClassLoader extends ClassLoader {
        private static final Enumeration<URL> NULL_ENUMERATION = new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public URL nextElement() {
                return null;
            }
        };

        static {
            registerAsParallelCapable();
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            throw new ClassNotFoundException(name);
        }

        @Override
        public URL getResource(String name) {
            return null;
        }

        @Override
        public Enumeration<URL> getResources(String var1) throws IOException {
            return NULL_ENUMERATION;
        }
    }
}
