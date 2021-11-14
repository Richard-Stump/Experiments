/**
 * This is a simple test program I wrote in order to test the idea of using Java's reflection features
 * inside a level editor. This program scans for subclasses of a type, and prints the properties
 * annotated with the @Editor annotation. This could be used to implement an editor system similar to
 * Unity or the Unreal engine.
 *
 * Feel free to use this however you want.
 */

package test;

import org.reflections.Reflections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * The main class to display the reflection data
 */
public class ReflectionTest {
    /**
     * This class represents a bunch of fields grouped together.
     */
    static class Group {
        public String name;
        public ArrayList<Field> fields; // A list of all fields inside this group

        public Group(String name) {
            this.name = name;
            this.fields = new ArrayList<>();
        }
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Class clazz = Parent.class;

        System.out.println("Parent: " + clazz.toString());

        //For the Reflections library to work, the class it is searching for needs to be inside a package.
        Reflections  reflections = new Reflections("test");

        //Get all the subclasses of Parent
        Set<Class<? extends Parent>> children = reflections.getSubTypesOf(Parent.class);

        //Loop through them
        for(Class<? extends Parent> c : children) {
            System.out.println(c.getName());
            System.out.println("Public Fields:");

            //Sort the fields in the current class by the name of their groups, which is part of the
            //@Editor annotation.
            Hashtable<String, Group> groups = new Hashtable<>();
            for(Field f : c.getFields()) {
                Editor fieldType = f.getDeclaredAnnotation(Editor.class);

                //Only add this field if an @Editor annotation is added to this field.
                if(fieldType != null) {
                    //Add a new group to the list if it does not exist.
                    if(!groups.containsKey(fieldType.group())) {
                        groups.put(fieldType.group(), new Group(fieldType.group()));
                    }

                    groups.get(fieldType.group()).fields.add(f);
                }
            }

            //Loop through the groups and print them.
            for(Group g : groups.values()) {
                //Don't print the name of fields inside the default group
                if(!g.name.equals("[unassigned]"))
                    System.out.println("\t" + g.name + ":");

                //For each of the fields in the group, print them to the console
                for(Field field : g.fields) {
                    if(g.name.equals("[unassigned]"))
                        System.out.println("\t" + field.getType() + ": " + field.getName());
                    else
                        System.out.println("\t\t" + field.getType() + ": " + field.getName());

                    Class fieldClass = field.getType();

                    //If this field is a normal class
                    if(!fieldClass.isPrimitive() && !fieldClass.isEnum()) {
                        Set<Class<?>> subClasses = reflections.getSubTypesOf(fieldClass);

                        //Print all the possible subclass this parameter could be
                        for(Class<?> sc : subClasses)
                            System.out.println("\t\t\t" + sc.getName());
                    }

                    //Print all the possible values this field could take if it is an enum
                    if(field.getType().isEnum()) {
                        Class enumType = field.getType();
                        Object[] values = enumType.getEnumConstants();

                        for(Object value : values)
                            System.out.println("\t\t\t" + value.toString());
                    }
                }

                System.out.println();
            }
        }
    }
}

/**
 * Annotation to mark specific fields as editable. Takes an optional parameter to group the fields together
 */
@Retention(RetentionPolicy.RUNTIME) //Needed to ensure the system works
@Target(ElementType.FIELD)
@interface Editor {
    String group() default "[unassigned]";
}

enum WaveType {
    NONE,
    SINE,
    COSINE,
    SAWTOOTH,
    SQUARE,
    CRAZY
}

/**
 * Class that any children must extend if they want the @Editor annotation to have an
 * effect. Any children derived will automatically get printed. Add new @Editor fields
 * and they will be too.
 */
class Parent {

}

class Child1 extends Parent {
    @Editor public float moveSpeed = 10.0f;
    @Editor public float jumpSpeed = 1.0f;

    @Editor(group = "Interactions") public int startLives = 3;
    @Editor(group = "Interactions") public int maxLives = 99;
}

class Child2 extends Parent {
    @Editor(group = "Properties") public int stackHeight = 5;
    @Editor(group = "Properties") public Parent stackClass;
    @Editor(group = "Properties") public WaveType stackWave = WaveType.SINE;
}