# Bridge Method Injector

## What's this?

When you are writing a library, there are various restrictions about the kind of changes you can make, in order to maintain binary compatibility.

One such restriction is an inability to restrict the return type. Say in v1 of your library you had the following code:

```java
public Foo getFoo() {
    return new Foo();
}
```

In v2, say if you introduce a subtype of `Foo` called `FooSubType`, and you want to change the getFoo method to return `FooSubType`.

```java
public FooSubType getFoo() {
    return new FooSubType();
}
```

But if you do this, you break the binary compatibility. The clients need to be recompiled to be able to work with the new signature. This is where this bridge method injector can help. By adding an annotation like the following:

```java
@WithBridgeMethods(Foo.class)
public FooSubType getFoo() {
    return new FooSubType();
}
```

... and running the bytecode post processor, your class file will get the additional "bridge methods." In pseudocode, it'll look like this:

```java
// your original definition
@WithBridgeMethods(Foo.class)
public FooSubType getFoo() {
    return new FooSubType();
}

// added bridge method
public Foo getFoo() {
    invokevirtual this.getFoo()LFooSubType;
    areturn
}
```

Such code isn't allowed in Java source files, but class files allow that. With this addition, existing clients will continue to function.

In this way, you can evolve your classes more easily without breaking backward compatibility.

## Widening the return type

In some cases, it's convenient to widen the return type of a method. As this is potentially a type-unsafe change
(as the callee can return a type that's not assignable to what the caller expects), so
you as a programmer explicitly need to tell us that you know what you are doing by adding
`castRequired` to the annotation.  For example, suppose that v1 had a method:

```java
public <T extends FooSubType> createFoo(Class<T> clazz) {
    return clazz.newInstance();
}
```

and in v2 you wanted to widen this method to. Note that you can prove that this is still type-safe, while
your compile cannot:

```java
public <T extends Foo> createFoo(Class<T> clazz) {
    return clazz.newInstance();
}
```

The annotation to provide backwards compatibility would be:

```java
@WithBridgeMethods(value=FooSubType.class, castRequired=true)
public <T extends Foo> createFoo(Class<T> clazz) {
    return clazz.newInstance();
}
```

Running the bytecode post processor, the resulting class file will look like the following pseudocode:

```java
// your original definition
@WithBridgeMethods(value=FooSubType.class, castRequired=true)
public <T extends Foo> createFoo(Class<T> clazz) {
    return clazz.newInstance();
}

// added bridge method
public FooSubType createFoo(Class clazz) {
    invokevirtual this.createFoo(java/lang/Class)LFoo
    checkcast FooSubType
    areturn
}
```

## Bridge methods and interfaces

You can use `@WithBridgeMethods` with interfaces, too. However, making this work correctly is tricky,
as you have to ensure that bridge methods are implemented on all the classes that implement the interface,
for example by adding `@WithBridgeMethods` on every implementation of the method in question,
or by introducing a base class that provides a bridge method.

See the Javadoc for more details:

- [`bridge-method-annotation`](https://javadoc.jenkins.io/component/bridge-method-annotation/)
- [`bridge-method-injector`](https://javadoc.jenkins.io/component/bridge-method-injector/)

## Maven repository

Starting with 1.25, this library is published at:

```xml
<repository>
  <id>repo.jenkins-ci.org</id>
  <url>https://repo.jenkins-ci.org/releases/</url>
</repository>
```

## Java support

Starting with 1.25, this library requires Java 11 or newer.

## Integration into your build

Add the following dependency in your POM. (This dependency is not needed at runtime, but it is necessary
for compilation of source code that transitively depend on this, so it is the simplest to just treat
this like a regular library dependency)

```xml
<dependency>
  <groupId>com.infradna.tool</groupId>
  <artifactId>bridge-method-annotation</artifactId>
  <version>1.25</version>
</dependency>
```

Then put the following fragment in your build to have the byte-code post processor kick in to inject the necessary bridge methods.

```xml
<build>
<plugins>
  <plugin>
    <groupId>com.infradna.tool</groupId>
    <artifactId>bridge-method-injector</artifactId>
    <version>1.25</version>
    <executions>
      <execution>
        <goals>
          <goal>process</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
</plugins>
</build>
```
