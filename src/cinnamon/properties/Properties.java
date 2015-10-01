/* 
 * Cinnamon Framework
 * Copyright (c) 2014, Andres Jaimes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of Cinnamon Framework nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package cinnamon.properties;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Properties was created to load and save POJO's to and from XML 
 * files in an easy way. Direct instantiation is performed so you don't have
 * to worry about types. 
 * 
 * Consider a file like the following:
 * 
 * <pre>{@code 
 * public class Properties {
 *     private String appName = "";
 * 
 *     public String getAppName() { return appName; }
 *     public void setAppName(String appName) { this.appName = appName; }
 * }
 * }</pre>
 * 
 * To persist this class to an XML file, just create an instance of the manager:
 * 
 * <pre>{@code 
 *     Properties<User> pm = new Properties<>("user.xml");
 }</pre>
 * 
 * and call its <code>load()</code> or <code>save()</code> methods:
 * 
 * <pre>{@code 
 *     pm.save(user); // save an instance of class User
 *     User user = pm.load(new User()); // load it from a file
 * }</pre>
 * 
 * The application supports the following types:
 *   boolean, Boolean, byte, Byte, Date, double, Double, float, Float, int, 
 *   Integer, long, Long, short, Short and String.
 * 
 * The application goes through all getters and setters when saving and 
 * loading respectively.
 * If a getter with a non-supported type is found, then it will be stored into
 * the file, but will not be retrieved.
 * 
 * @author Andres Jaimes
 * @version 1.0
 * @param <T> the generic type to use.
 */
public class Properties<T extends Object> {
       
    private final File __file;
    private final String __dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final String __rootName = "properties";
    private final String __attributeType = "type";
    private final String __attributeValue = "value";
    private final String __boolean = "boolean";
    private final String __byte = "byte";
    private final String __double = "double";
    private final String __float = "float";
    private final String __int = "int";
    private final String __long = "long";
    private final String __short = "short";
    private final String __classBoolean = "java.lang.Boolean";
    private final String __classByte = "java.lang.Byte";
    private final String __classDate = "java.util.Date";
    private final String __classDouble = "java.lang.Double";
    private final String __classFloat = "java.lang.Float";
    private final String __classInt = "java.lang.Integer";
    private final String __classLong = "java.lang.Long";
    private final String __classShort = "java.lang.Short";
    private final String __classString = "java.lang.String";
    

    /** 
     * Creates a new instance of this class. 
     * @param file the file to use.
     */    
    public Properties(File file) {
        __file = file;
    }

    
    /** 
     * Creates a new instance of this class. 
     * @param fileName the file name to use.
     */    
    public Properties(String fileName) {
        __file = new File(fileName);
    }
    
    
    /**
     * Looks for the corresponding file name and initializes properties values
     * from it.
     * @param instance the class instance to be populated from file.
     * @return a populated instance with properties read from file.
     */
    public T load(T instance) {
                
        if (__file.exists()) {
            
            try {
                DocumentBuilder db = 
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
                Document doc = db.parse(__file);
                if (doc.hasChildNodes()) {
                    Node root = doc.getChildNodes().item(0);
                    if (root != null && root.hasChildNodes()) {
                        populate(root.getChildNodes(), instance);
                    }
                }
            }
            // report any issue
            catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, ex);
            }
        }
        
        return instance;
        
    }
    
    
    /**
     * Populates all properties by using reflection.
     * @param nodeList 
     */
    private void populate(NodeList nodeList, T instance) {
       
        for (int i = 0; i < nodeList.getLength(); i++) {
            
            Node node = nodeList.item(i);
            
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                
                Element element = (Element) node;
                String setter = getSetterName(node.getNodeName());
                String type = element.getAttribute(__attributeType);
                String value;
                if (__classString.equals(type)) {
                    Node child = element.getFirstChild();
                    if (child != null && child instanceof CharacterData) {
                        CharacterData cdata = (CharacterData) child;
                        value = cdata.getData();
                    }
                    else value = "";
                }
                else {
                    value = element.getAttribute(__attributeValue);
                }
                
                // Look for an appropiate setter.
                Method method = findSetter(instance, setter, type);
                if (method != null) {
                    try {
                        // Call the setter.
                        method.setAccessible(true);
                        method.invoke(instance, toObject(type, value));
                    }
                    catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        // If it finds a problem, ignore and go with the next property
                    }
                }
                
            }
            
        }
        
    }

    
    /**
     * Returns the name of a setter for a given property name, for example: 
     * if parameter propertyName = "id" this function will return "setId".
     * @param propertyName the property name.
     * @return a value representing a setter name for a given property.
     */
    private String getSetterName(String propertyName) {
        String setter = "set";
        
        if (propertyName != null & propertyName.length() > 0) {
            setter += propertyName.substring(0, 1).toUpperCase() + 
                    propertyName.substring(1);
        }
        
        return setter;
    }
    
    
    /**
     * Uses reflection to find an appropriate method setter.
     * @param setter The setter name to look for, ie: "setId"
     * @param type The type of the setter parameter, ie: "double"
     * @return a Method referencing the setter or null if not found.
     */
    private Method findSetter(T instance, String setter, String type) {
        // Get reference to current type
        Class theClass = instance.getClass();
        
        Method method = null;
        
        try {
            switch (type) {
                case __boolean:
                    method = theClass.getDeclaredMethod(setter, boolean.class); break;
                case __classBoolean: 
                    method = theClass.getDeclaredMethod(setter, Boolean.class); break;
                case __byte:
                    method = theClass.getDeclaredMethod(setter, byte.class); break;            
                case __classByte: 
                    method = theClass.getDeclaredMethod(setter, Byte.class); break;            
                case __classDate:                 
                    method = theClass.getDeclaredMethod(setter, Date.class); break;
                case __double:
                    method = theClass.getDeclaredMethod(setter, double.class); break;                
                case __classDouble: 
                    method = theClass.getDeclaredMethod(setter, Double.class); break;                
                case __float:
                    method = theClass.getDeclaredMethod(setter, float.class); break;
                case __classFloat: 
                    method = theClass.getDeclaredMethod(setter, Float.class); break;
                case __int:
                    method = theClass.getDeclaredMethod(setter, int.class); break;
                case __classInt: 
                    method = theClass.getDeclaredMethod(setter, Integer.class); break;
                case __long:
                    method = theClass.getDeclaredMethod(setter, long.class); break;
                case __classLong: 
                    method = theClass.getDeclaredMethod(setter, Long.class); break;
                case __short:
                    method = theClass.getDeclaredMethod(setter, short.class); break;
                case __classShort: 
                    method = theClass.getDeclaredMethod(setter, Short.class); break;
                case __classString: 
                    method = theClass.getDeclaredMethod(setter, String.class); break;
            }
        }
        catch (NoSuchMethodException ex) {
            // If not found, then we'll return null
        }
        
        return method;
    }    
    
    
    /**
     * Turns a String value into the corresponding java type value.
     * @param type the target type.
     * @param value the value to convert.
     * @return an object representation of the value.
     */
    private Object toObject(String type, String value) {
        Object object = null;
        
        try {
            switch (type) {
                case __boolean:
                case __classBoolean: 
                    object = Boolean.parseBoolean(value); break;
                case __byte:
                case __classByte: 
                    object = Byte.parseByte(value); break;
                case __classDate: 
                    object = new SimpleDateFormat(__dateFormat).parse(value); break;
                case __double:
                case __classDouble: 
                    object = Double.parseDouble(value); break;
                case __float:
                case __classFloat: 
                    object = Float.parseFloat(value); break;
                case __int:
                case __classInt:
                    object = Integer.parseInt(value); break;
                case __long:
                case __classLong: 
                    object = Long.parseLong(value); break;
                case __short:
                case __classShort: 
                    object = Short.parseShort(value); break;
                case __classString: 
                    object = value;
            }
        }
        catch (NumberFormatException | ParseException ex) {
            // If not found, then we'll return null
        }
        
        return object;
    }
    
    
    /**
     * Saves all the instance's properties into a file. 
     * @param instance the class instance to save.
     */
    public final void save(T instance) {
        try {
            DocumentBuilder db = 
                DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = db.newDocument();
            Element root = doc.createElement(__rootName);
            doc.appendChild(root);
            
            // Get reference to current type
            Class theClass = instance.getClass();
            
            // Iterate the "getters" to get the properties to save.
            Method[] getters = theClass.getDeclaredMethods();
            for (Method getter : getters) {
                if (isGetter(getter)) {
                    // Create each property element and its attributes
                    Element element = doc.createElement(getPropertyName(getter.getName()));
                    element.setAttribute(__attributeType, getter.getReturnType().getName());
                    if (__classString.equals(getter.getReturnType().getName())) {
                        CDATASection cdata = doc.createCDATASection(resultToString(instance, getter));
                        element.appendChild(cdata);                        
                    }
                    else {
                        element.setAttribute(__attributeValue, resultToString(instance, getter));
                    }
                    root.appendChild(element);
                }
            }
            
            saveToFile(doc);
            
        }
        catch (ParserConfigurationException | DOMException | SecurityException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
            
    /**
     * Returns the name of a property for a given get name, for example: 
     * if parameter getterName = "getId" this function will return "id".
     * @param getterName the method name.
     * @return a value representing a property name for a given get.
     */
    private String getPropertyName(String getterName) {
        return Introspector.decapitalize(getterName.substring(getterName.startsWith("is") ? 2 : 3));
    }
    
    
    /**
     * Calls the given get and converts its return value into a string.
     * @param getter The method from which the value will be obtained.
     * @return a string representation of the value obtained from the get
     *         or an empty string if something goes wrong.
     */
    private String resultToString(T instance, Method getter) {
        
        String s = "";
        
        try {            
            getter.setAccessible(true);
            
            switch (getter.getReturnType().getName()) {
                case __classDate: 
                    Date d = (Date) getter.invoke(instance);
                    s = new SimpleDateFormat(__dateFormat).format(d); break;
                default: 
                    s = getter.invoke(instance).toString();
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // return an empty string...
        }
        
        return s;
    }
    
    
    /** 
     * Performs the actual write of the file to disk.
     * @param doc the in-memory xml document to save.
     */
    private void saveToFile(Document doc) {
        try {
            // Prepare to write the content into a file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(__file);
            
            // set options for the resulting file
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            
            // and save it...
            transformer.transform(source, result);
        } catch (IllegalArgumentException | TransformerException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /**
     * Checks if a given method is a getter.
     * @param method the method to check.
     * @return true if it is a getter, false otherwise.
     */
    private boolean isGetter(Method method){
        String name = method.getName();
        return
            (name.startsWith("get") && name.length() > 3 
            || name.startsWith("is") && name.length() > 2) 
            && method.getParameterTypes().length == 0
            && method.getReturnType() != void.class;
    }

}
