# Cinnamon Properties

Properties is a class that helps you save and load a class's properties 
to and from XML files in an easy way. Since it uses reflection, you don't 
have to worry about type casting. 
 
Consider a class like the following:
 
	public class User {
		private int id;
	    private String name = "";
	 
	    public int getId() { return id; }
	    public void setId(int id) { this.id = id; }

	    public String getName() { return name; }
	    public void setName(String name) { this.name = name; }
	}

To persist this class into an XML file, just create an instance of Properties
and specify the file name you want to use:
 
	Properties<User> pm = new Properties<>("user.xml");

To save an instance, call the `save()` method:
 
 	User user = new User();
 	user.setId(1);
 	user.setName("Yoyo");
	pm.save(user); // save an instance of class User

To load an instance, call the `load()` method:

    User user = pm.load(new User()); // load it from a file
    System.out.println(user.getName());

The class supports the following types: `boolean`, `Boolean`, `byte`, 
`Byte`, `Date`, `double`, `Double`, `float`, `Float`, `int`, `Integer`, `long`,
`Long`, `short`, `Short` and `String`.

In order to save or load, it goes through all getters and setters when saving and 
loading. If a getter with a non-supported type is found, then it will be stored into
the file, but will not be retrieved.
