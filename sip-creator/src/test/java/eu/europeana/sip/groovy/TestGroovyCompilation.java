package eu.europeana.sip.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.System.out;

/**
 * todo: add class description
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */

@Ignore 
public class TestGroovyCompilation {

    public TestGroovyCompilation() {
        GroovyShell groovyShell = new GroovyShell();
        GroovyClassLoader groovyClassLoader = groovyShell.getClassLoader();
        Class gd = groovyClassLoader.parseClass("GroovyDraft.groovy");
        System.out.printf("%s%n", gd);
        Constructor[] c = gd.getConstructors();
        System.out.printf("* Count of constructors %d%n", c.length);
        Method[] methods = gd.getMethods();
        out.printf("* Methods *%n");
        print(methods);
        out.printf("Constructors%n");
        print(c);
    }

    public static void main(String... args) {
        new TestGroovyCompilation();
    }

    public static void print(Object[] o) {
        for (Object ob : o) {
            System.out.printf("%s%n", ob);
            if (ob.toString().contains("tellName")) {
                System.out.printf("%n%n->%10s<-%n%n", ob);
            }
        }
    }

    /**
     * Compiling a Groovy snippet
     */
    private static class ScriptCompiler {

        private ScriptCompiler() {
            GroovyShell groovyShell = new GroovyShell();
            Script myScript;
            try {
                myScript = groovyShell.parse(new File("sip-creator/src/test/java/eu/europeana/sip/groovy/Groove.groovy"));
                myScript.run();
                myScript.invokeMethod("normalize", "test from java");
                out.printf("* Constructors for Groove *%n");
                print(myScript.getClass().getConstructors());
                out.printf("* Methods for Groove *%n");
                print(myScript.getClass().getMethods());
                print(myScript.getClass().getAnnotations());

            }
            catch (IOException e) {
                e.printStackTrace();  // todo: handle catch
            }
        }

        public static void main(String... args) {
            new ScriptCompiler();
        }
    }

    /**
     * Compiling GroovyDraft.groovy to Java byte-code
     */
    private static class FileCompiler {
        private FileCompiler() {
            GroovyShell groovyShell = new GroovyShell();
            GroovyClassLoader groovyClassLoader = groovyShell.getClassLoader();
            try {
                System.out.printf("%s%n", new File(".").getAbsolutePath());
                Class clazz = groovyClassLoader.parseClass(new File("sip-creator/src/test/java/eu/europeana/sip/groovy/GroovyDraft.groovy"));
                Object o = clazz.newInstance();
                System.out.printf("%s%n", clazz);
                print(clazz.getMethods());
                Method method = clazz.getMethod("tellName");
                System.out.printf("Method %s%n", method);
                System.out.printf("%s%n", clazz);
                String result = (String) method.invoke(o);
                System.out.printf("%s%n", result);
            }
            catch (IOException e) {
                e.printStackTrace();  // todo: handle catch
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();  // todo: handle catch
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();  // todo: handle catch
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();  // todo: handle catch
            }
            catch (InstantiationException e) {
                e.printStackTrace();  // todo: handle catch
            }
        }

        public static void main(String... args) {
            new FileCompiler();
        }
    }
}
