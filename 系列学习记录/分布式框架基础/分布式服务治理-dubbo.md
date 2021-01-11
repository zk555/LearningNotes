### 一 、Dubbo 的架构

​		单系统架构：

![1601368867929](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601368867929.png)

#### 1.1 架构升级带来哪些问题

​		（1） 当服务越来越多时，服务 URL 配置管理变得非常困难，F5 硬件负载均衡器的单点压力也越来越大。	

并通过在消费方获取服务提供方地址列表，实现软负载均衡和 Failover，降低对 F5 硬件负载均衡器的依赖，也能

减少部分成本。

**此时需要一个服务注册中心，动态的注册和发现服务，使服务的位置透明。**

​		（2） 当进一步发展，服务间依赖关系变得错踪复杂，甚至分不清哪个应用要在哪个应用之前启动，架构师都不能完整的描述应用的架构关系。

**这时，需要自动画出应用间的依赖关系图，以帮助架构师理清理关系。**

​		（3） 服务的调用量越来越大，服务的容量问题就暴露出来，这个服务需要多少机器支撑？什么时候该加机器？

​		为了解决这些问题，第一步，要将服务现在每天的调用量，响应时间，都统计出来，作为容量规划的参考指标。其次，要可以动态调整权重，在线上，将某台机器的权重一直加大，并在加大的过程中记录响应时间的变化，直到响应时间到达阀值，记录此时的访问量，再以此访问量乘以机器数反推总容量。

#### 1.2  架构模型

![1601369226838](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601369226838.png)

### 二、 Dubbo 案例演示

​		![1601369302232](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601369302232.png)

### 三 、常用文件配置解析

#### 		3.1  多版本支持

##### 3.1.1 程序配置方式

```xml
<dubbo:service interface="com.gupaoedu.dubbo.IGpHello"
       ref="demoService2" protocol="dubbo" version="1.0.1" timeout="100"/>
```

​	在zookeeper注册中心上的体现形式：

```shell
dubbo://192.168.11.1:20880%2Fcom.gupaoedu.dubbo.IGpHello%3Fanyhost%3Dtrue%26application%3Dhello-world-app%26dubbo%3D2.5.6%26generic%3Dfalse%26interface%3Dcom.gupaoedu.dubbo.IGpHello%26methods%3DsayHello%26pid%3D60700%26revision%3D1.0.0%26side%3Dprovider%26timestamp%3D1529498478644%26version%3D1.0.0

dubbo://192.168.11.1%3A20880%2Fcom.gupaoedu.dubbo.IGpHello2%3Fanyhost%3Dtrue%26application%3Dhello-world-app%26dubbo%3D2.5.6%26generic%3Dfalse%26interface%3Dcom.gupaoedu.dubbo.IGpHello%26methods%3DsayHello%26pid%3D60700%26revision%3D1.0.1%26side%3Dprovider%26timestamp%3D1529498488747%26version%3D1.0.1

```

#### 3.2 主机绑定

​			在发布一个Dubbo服务的时候，会生成一个dubbo://ip:port的协议地址，那么这个IP是根据什么生成的呢？大家可以在ServiceConfig.java代码中找到如下代码;可以发现，在生成绑定的主机的时候，会通过一层一层的判断，直到获取到合法的ip地址。

​			1. NetUtils.isInvalidLocalHost(host)， 从配置文件中获取host

​			2. host = InetAddress.getLocalHost().getHostAddress();

​			3. 

```java
	Socket socket = new Socket();
try {
    SocketAddress addr = new InetSocketAddress(registryURL.getHost(), registryURL.getPort());
    socket.connect(addr, 1000);
    host = socket.getLocalAddress().getHostAddress();
    break;
} finally {
    try {
        socket.close();
    } catch (Throwable e) {}
}
```

  		4. ![1601384012562](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601384012562.png)



#### 3.3 集群容错

#####  3.3.1 什么是容错机制？

​		 容错机制指的是某种系统控制在一定范围内的一种允许或包容犯错情况的发生，举个简单例子，我们在电脑上运行一个程序，有时候会出现无响应的情况，然后系统会弹出一个提示框让我们选择，是立即结束还是继续等待，然后根据我们的选择执行对应的操作，这就是“容错”。

##### 3.3.2 Dubbo提供了6种容错机制

```java
1.	failsafe 失败安全，可以认为是把错误吞掉（记录日志）
2.	failover(默认)   重试其他服务器； retries（2）
3.	failfast 快速失败， 失败以后立马报错
4.	failback  失败后自动恢复。
5.	forking  forks. 设置并行数
6.	broadcast  广播，任意一台报错，则执行的方法报错
```

配置方式如下，通过cluster方式，配置指定的容错方案

![1601385023435](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601385023435.png)

#### 3.4 服务降级

降级可以有几个层面的分类：自动降级和人工降级； 按照功能可以分为：读服务降级和写服务降级

- 对一些非核心服务进行人工降级，在大促之前通过降级开关关闭哪些推荐内容、评价等对主流程没有影响的功能
- 故障降级，比如调用的远程服务挂了，网络故障、或者RPC服务返回异常。 那么可以直接降级，降级的方案比如设置默认值、采用兜底数据（系统推荐的行为广告挂了，可以提前准备静态页面做返回）等等
- 限流降级，在秒杀这种流量比较集中并且流量特别大的情况下，因为突发访问量特别大可能会导致系统支撑不了。这个时候可以采用限流来限制访问量。当达到阀值时，后续的请求被降级，比如进入排队页面，比如跳转到错误页（活动太火爆，稍后重试等）

##### 3.4.1 dubbo的降级方式: Mock

实现步骤:

```xml
1.	在client端创建一个TestMock类，实现对应IGpHello的接口（需要对哪个接口进行mock，就实现哪个），名称必须以Mock结尾
2.	在client端的xml配置文件中，添加如下配置，增加一个mock属性指向创建的TestMock
3.	模拟错误（设置timeout），模拟超时异常，运行测试代码即可访问到TestMock这个类。当服务端故障解除以后，调用过程将恢复正常
```

配置：

```xml
<!-- 声明需要暴露的服务接口 --><dubbo:reference id="demoService"                 interface="com.gupaoedu.dubbo.IGpHello" registry="zookeeper"                 mock="com.gupaoedu.dubbo.TestMock"/>
```

#### 3.5 配置优先级别

以timeout为例，显示了配置的查找顺序，其它retries, loadbalance等类似。

- 方法级优先，接口级次之，全局配置再次之。

-  如果级别一样，则消费方优先，提供方次之。

其中，服务提供方配置，通过URL经由注册中心传递给消费方。

​		建议由服务提供方设置超时，因为一个方法需要执行多长时间，服务提供方更清楚，如果一个消费方同时引用多个服务，就不需要关心每个服务的超时设置。

### 四、DubboSPI和JdkSPI的对比

​	在Dubbo中，SPI是一个非常核心的机制，贯穿在几乎所有的流程中。

#### 4.1 关于JAVA 的SPI机制

​		SPI全称（serviceprovider interface），是JDK内置的一种服务提供发现机制，目前市面上有很多框架都是用它来做服务的扩展发现，大家耳熟能详的如JDBC、日志框架都有用到；

​		简单来说，它是一种动态替换发现的机制。举个简单的例子，如果我们定义了一个规范，需要第三方厂商去实现，那么对于我们应用方来说，只需要集成对应厂商的插件，既可以完成对应规范的实现机制。形成一种插拔式的扩展手段。

#### 4.2 实现一个SPI机制

​		实现的代码的流程图如下：

![1601385869389](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601385869389.png)

####  4.3 SPI规范总结

​		实现SPI，就需要按照SPI本身定义的规范来进行配置，SPI规范如下

- 需要在classpath下创建一个目录，该目录命名必须是：META-INF/services
- 在该目录下创建一个properties文件，该文件需要满足以下几个条件
  -  文件名必须是扩展的接口的全路径名称
  -  文件内部描述的是该扩展接口的所有实现类
  -  文件的编码格式是UTF-8
- 通过java.util.ServiceLoader的加载机制来发现

#### 4.4 SPI的实际应用

​			SPI在很多地方有应用，大家可以看看最常用的java.sql.Driver驱动。JDK官方提供了java.sql.Driver这个驱动扩展点，但是你们并没有看到JDK中有对应的Driver实现。

​			以连接Mysql为例，我们需要添加mysql-connector-java依赖。然后，你们可以在这个jar包中找到SPI的配置信息。如下图，所以java.sql.Driver由各个数据库厂商自行实现。这就是SPI的实际应用。

![1601386049606](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601386049606.png)

#### 4.5 SPI的缺点

- JDK标准的SPI会一次性加载实例化扩展点的所有实现，什么意思呢？

  ​		就是如果你在META-INF/service下的文件里面加了N个实现类，那么JDK启动的时候都会一次性全部加载。那么如果有的扩展点实现初始化很耗时或者如果有些实现类并没有用到，那么会很浪费资源

- 如果扩展点加载失败，会导致调用方报错，而且这个错误很难定位到是这个原因
- 多个并发多线程使用 ServiceLoader 类的实例是不安全的。

#### 4.6 Dubbo优化后的SPI实现

##### 	4.6.1 基于Dubbo提供的SPI规范实现自己的扩展

​		在了解Dubbo的SPI机制之前，先通过一段代码初步了解Dubbo的实现方式，这样，我们就能够形成一个对比，得到这两种实现方式的差异

##### 	4.6.2 Dubbo的SPI机制规范

​		大部分的思想都是和SPI是一样，只是下面两个地方有差异。

- 需要在resource目录下配置META-INF/dubbo或者META-INF/dubbo/internal或者META-INF/services，并基于SPI接口去创建一个文件
- 文件名称和接口名称保持一致，文件内容和SPI有差异，内容是KEY对应Value



### 五 、Dubbo SPI机制源码阅读

```java
1.Protocol protocol = ExtensionLoader. getExtensionLoader(Protocol.class).getExtension("myProtocol");
2.Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();
```

​		ps: 源码阅读，带着疑问去了解【为什么传入一个myProtocol就能获得自定义的DefineProtocol对象】、 getAdaptiveExtension是一个什么东西？

#### 5.1 源码阅读入口

​			接下来的源码分析，是基于下面这段代码作为入口，至于为什么不用上面提到的第一段代码作为入口。理由如下

```java
Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class). getAdaptiveExtension();
```

​		把上面这段代码分成两段，一段是getExtensionLoader、 另一段是getAdaptiveExtension。

初步猜想一下；

- 第一段是通过一个Class参数去获得一个ExtensionLoader对象，有点类似一个工厂模式。
- 第二段getAdaptiveExtension，去获得一个自适应的扩展点

#### 5.2 Extension源码的结构

了解源码结构，建立一个全局认识。结构图如下

![1601386801877](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601386801877.png)

#### 5.3 Protocol源码

```java
@SPI("dubbo")
public interface Protocol {
    
    /**
     * 获取缺省端口，当用户没有配置端口时使用。
     * 
     * @return 缺省端口
     */
    int getDefaultPort();

    /**
     * 暴露远程服务：<br>
     * 1. 协议在接收请求时，应记录请求来源方地址信息：RpcContext.getContext().setRemoteAddress();<br>
     * 2. export()必须是幂等的，也就是暴露同一个URL的Invoker两次，和暴露一次没有区别。<br>
     * 3. export()传入的Invoker由框架实现并传入，协议不需要关心。<br>
     * 
     * @param <T> 服务的类型
     * @param invoker 服务的执行体
     * @return exporter 暴露服务的引用，用于取消暴露
     * @throws RpcException 当暴露服务出错时抛出，比如端口已占用
     */
    @Adaptive
    <T> Exporter<T> export(Invoker<T> invoker) throws RpcException;

    /**
     * 引用远程服务：<br>
     * 1. 当用户调用refer()所返回的Invoker对象的invoke()方法时，协议需相应执行同URL远端export()传入的Invoker对象的invoke()方法。<br>
     * 2. refer()返回的Invoker由协议实现，协议通常需要在此Invoker中发送远程请求。<br>
     * 3. 当url中有设置check=false时，连接失败不能抛出异常，并内部自动恢复。<br>
     * 
     * @param <T> 服务的类型
     * @param type 服务的类型
     * @param url 远程服务的URL地址
     * @return invoker 服务的本地代理
     * @throws RpcException 当连接服务提供方失败时抛出
     */
    @Adaptive
    <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException;
    
     /**
     * 释放协议：<br>
     * 1. 取消该协议所有已经暴露和引用的服务。<br>
     * 2. 释放协议所占用的所有资源，比如连接和端口。<br>
     * 3. 协议在释放后，依然能暴露和引用新的服务。<br>
     */
    void destroy();
```

在这个源码中可以看到有两个注解，一个是在类级别上的@SPI(“dubbo”).另一个是@Adaptive

- @SPI 表示当前这个接口是一个扩展点，可以实现自己的扩展实现，默认的扩展点是DubboProtocol。
- @Adaptive  表示一个自适应扩展点，
  - 方法：在方法级别上，会动态生成一个适配器类
  - 类：直接返回自适应类

##### 5.3.1 getExtensionLoader

```java
public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if(!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if(!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type + 
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }
        
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }
```

​			该方法需要一个Class类型的参数，该参数表示希望加载的扩展点类型，该参数必须是接口，且该接口必须

被@SPI注解注释，否则拒绝处理。

​			检查通过之后首先会检查ExtensionLoader缓存中是否已经存在该扩展对应的ExtensionLoader，如果有则

直接返回，否则创建一个新的ExtensionLoader负责加载该扩展实现，同时将其缓存起来。可以看到对于每一个扩

展，dubbo中只会有一个对应的ExtensionLoader实例

##### 5.3.2 getAdaptiveExtension

​			通过getExtensionLoader获得了对应的ExtensionLoader实例以后，再调用getAdaptiveExtension()方法来

获得一个自适应扩展点。

**ps：简单对自适应扩展点做一个解释，大家一定了解过适配器设计模式，而这个自适应扩展点实际上就是一个适配器。**

```java
 public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if(createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            }
            else {
                throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }

        return (T) instance;
    }
```

这个方法里面主要做几个事情：

1. 从cacheAdaptiveInstance 这个内存缓存中获得一个对象实例

2. 如果实例为空，说明是第一次加载，则通过双重检查锁的方式去创建一个适配器扩展点

##### 5.3.3 createAdaptiveExtension

```java
 private T createAdaptiveExtension() {
        try {
            //可以实现扩展点的注入
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extenstion " + type + ", cause: " + e.getMessage(), e);
        }
    }
```

​			先去了解getAdaptiveExtensionClass这个方法做了什么？很显然，从后面的.newInstance来看，应该是获得一个类并且进行实例)

##### 5.3.4 getAdaptiveExtensionClass

```java
private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses(); //加载所有路径的扩展点
        //TODO  不一定？
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass; //AdaptiveCompiler
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass(); //创建对象
    }
```

1. getExtensionClasses() 加载所有路径下的扩展点

2. createAdaptiveExtensionClass() 动态创建一个扩展点

cachedAdaptiveClass这里有个判断，用来判断当前Protocol这个扩展点是否存在一个自定义的适配器，如果有，则直接返回自定义适配器，否则，就动态创建，这个值是在getExtensionClasses中赋值的，这块代码我们稍后再看

##### 5.3.5 createAdaptiveExtensionClass

```java
 //创建一个适配器扩展点。（创建一个动态的字节码文件）
    private Class<?> createAdaptiveExtensionClass() {
        //生成字节码代码
        String code = createAdaptiveExtensionClassCode();
        //获得类加载器
        ClassLoader classLoader = findClassLoader();
        Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
        //动态编译字节码
        return compiler.compile(code, classLoader);
    }
```

动态生成适配器代码，以及动态编译

1. createAdaptiveExtensionClassCode,  动态创建一个字节码文件。返回code这个字符串

2. 通过compiler.compile进行编译（默认情况下使用的是javassist）

3. 通过ClassLoader加载到jvm中

##### 5.3.6 code的字节码内容

```java
public class Protocol$Adaptive implements com.alibaba.dubbo.rpc.Protocol {
    public void destroy() {
        throw new UnsupportedOperationException("method public abstract void com.alibaba.dubbo.rpc.Protocol.destroy() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
    }

    public int getDefaultPort() {
        throw new UnsupportedOperationException("method public abstract int com.alibaba.dubbo.rpc.Protocol.getDefaultPort() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
    }

    public Invoker refer(java.lang.Class arg0, com.alibaba.dubbo.common.URL arg1) throws RpcException {
        if (arg1 == null) throw new IllegalArgumentException("url == null");
        URL url = arg1;
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url(" + url.toString() + ") use keys([protocol])");
        Protocol extension = (Protocol) ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);
        return extension.refer(arg0, arg1);
    }

    public com.alibaba.dubbo.rpc.Exporter export(Invoker arg0) throws RpcException {
        if (arg0 == null) throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument == null");
        if (arg0.getUrl() == null)
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument getUrl() == null");
        URL url = arg0.getUrl();
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url(" + url.toString() + ") use keys([protocol])");
        //得到指定协议的实现类
        Protocol extension = (Protocol) ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);
        return extension.export(arg0);
    }
}
```

Protocol$Adaptive的主要功能

1. 从url或扩展接口获取扩展接口实现类的名称； 

2. 根据名称，获取实现类ExtensionLoader.getExtensionLoader(扩展接口类).getExtension(扩展接口实现类名

   称)，然后调用实现类的方法。

3. dubbo的内部传参基本上都是基于Url来实现的，也就是说Dubbo是基于URL驱动的技术所以，适配器类的目

   的是在运行期获取扩展的真正实现来调用，解耦接口和实现，这样的话要不我们自己实现适配器类，要dubbo

   帮我们生成，而这些都是通过Adpative来实现。

4.  到这里我们要理解到 dubbo为什么要动态生成一个Protocol$Adaptive，它的意义再于可以根据《extName》来动态的加载MET-INF/services中的配置实现类

AdaptiveExtension的主线走完了，可以简单整理一下他们的调用关系如下

![1601388902100](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601388902100.png)



​		在调用createAdaptiveExtensionClass之前，还做了一个操作。是执行getExtensionClasses方法，我们来看看这个方法做了什么事情

##### 5.3.7 getExtensionClasses

```java
//加载扩展点的实现类
	private Map<String, Class<?>> getExtensionClasses() {

        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
	}
```

这段代码主要做如下几个事情

1. 从cachedClasses中获得一个结果，这个结果实际上就是所有的扩展点类，key对应name，value对应class

2. 通过双重检查锁进行判断

3. 调用loadExtensionClasses，去加载左右扩展点的实现

##### 5.3.8 loadExtensionClasses

```java
// 此方法已经getExtensionClasses方法同步过。
    private Map<String, Class<?>> loadExtensionClasses() {
        //type->Protocol.class
        //得到SPI的注解
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if(defaultAnnotation != null) { //如果不等于空.
            String value = defaultAnnotation.value();
            if(value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if(names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if(names.length == 1) cachedDefaultName = names[0];
            }
        }
        
        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadFile(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadFile(extensionClasses, DUBBO_DIRECTORY);
        loadFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }
```

​		从不同目录去加载扩展点的实现，在最开始的时候讲到过的。META-INF/dubbo ；META-INF/internal ;
META-INF/services

主要逻辑:

1. 获得当前扩展点的注解，也就是Protocol.class这个类的注解，@SPI

2. 判断这个注解不为空，则再次获得@SPI中的value值

3. 如果value有值，也就是@SPI(“dubbo”)，则讲这个dubbo的值赋给cachedDefaultName。这就是为什么我们能够通过ExtensionLoader.getExtensionLoader(Protocol.class).getDefaultExtension() ,能够获得DubboProtocol这个扩展点的原因

4. 最后，通过loadFile去加载指定路径下的所有扩展点。也就是META-INF/dubbo;META-INF/internal;META-INF/services

##### 5.3.9 loadFile

​		解析指定路径下的文件，获取对应的扩展点，通过反射的方式进行实例化以后，put到extensionClasses这个Map集合中

代码略；

##### 5.3.10  injectExtension

思考： 扩展点中用到其它扩展点该怎么使用？

```java
 //依赖注入 （）
    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (method.getName().startsWith("set") // 有 set方法
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }
```

Object object = **objectFactory**.getExtension(pt, property);

在获取扩展点后执行上述方法，进行依赖注入，通过工厂方式创建Extension对象

```java
 			injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (wrapperClasses != null && wrapperClasses.size() > 0) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
```

依赖注入完成后，进行wrapperClass包装，那么包装的是什么东西?

```XML
filter=com.alibaba.dubbo.rpc.protocol.ProtocolFilterWrapper
listener=com.alibaba.dubbo.rpc.protocol.ProtocolListenerWrapper
```

监听器，过滤器 

##### 5.3.11 流程图

![1601438635521](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601438635521.png)

​		扩展点自动注入的一句就是根据 setter 方法对应的参数类型和 property 名称从 ExtensionFactory 中查询，如果有返回扩展点实例，那么就进行注入操作。



**AdaptiveCompiler**

```java
@Adaptive
public class AdaptiveCompiler implements Compiler {

    private static volatile String DEFAULT_COMPILER;

    public static void setDefaultCompiler(String compiler) {
        DEFAULT_COMPILER = compiler;
    }

    public Class<?> compile(String code, ClassLoader classLoader) {
        Compiler compiler;
        ExtensionLoader<Compiler> loader = ExtensionLoader.getExtensionLoader(Compiler.class);
        String name = DEFAULT_COMPILER; // copy reference
        if (name != null && name.length() > 0) {
            compiler = loader.getExtension(name);
        } else {
            compiler = loader.getDefaultExtension();
        }
        return compiler.compile(code, classLoader);
    }

}
```

​			这个类里面有一个 setDefaultCompiler 方法，他本身没有实现 compile。而是基于 DEFAULT_COMPILER。然后加载指定扩展点进行动态调用。那么这个 DEFAULT_COMPILER 这个值，就是在 injectExtension 方法中进行注入的

##### 5.3.12 关于 objectFactory

```java
private ExtensionLoader(Class<?> type) {//PrgetAdaptiveExtensionotocol.class
        this.type = type;
        objectFactory = (type == ExtensionFactory.class ? null :
                ExtensionLoader.getExtensionLoader(ExtensionFactory.class).
                        getAdaptiveExtension());
    }
```

​			ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension()去获得一个自适应的扩展点，进入 ExtensionFactory 这个接口中，可以看到它是一个扩展点，并且有一个自己实现的自适应扩展点AdaptiveExtensionFactory; 

​			注意：@Adaptive 加载到类上表示这是一个自定义的适配器类，表示我们再调用 getAdaptiveExtension 方法的时候，不需要走上面这么复杂的过程。会直接加载到 AdaptiveExtensionFactory。

![1601439031292](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601439031292.png)`

```java
 public AdaptiveExtensionFactory() {
        ExtensionLoader<ExtensionFactory> loader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);
        List<ExtensionFactory> list = new ArrayList<ExtensionFactory>();
        for (String name : loader.getSupportedExtensions()) {
            list.add(loader.getExtension(name));
        }
        factories = Collections.unmodifiableList(list);
    }

public <T> T getExtension(Class<T> type, String name) {
        for (ExtensionFactory factory : factories) {
            T extension = factory.getExtension(type, name);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }
```

我们可以看到除了自定义的自适应适配器类以外，还有两个实现类，一个
是 SPI，一个是 Spring，AdaptiveExtensionFactory
AdaptiveExtensionFactory 轮询这 2 个，从一个中获取到就返回



### 六、 服务端发布流程



#### 6.1  Spring对外部的扩展接口

​			dubbo 是基于 spring 配置来实现服务的发布的，那么一定是基于 spring的扩展来写了一套自己的标签，那么 spring 是如何解析这些配置呢？

​			在dubbo 配置文件中看到的<dubbo:service> ，就是属于自定义扩展标签要实现自定义扩展，有三个步骤（在 spring 中定义了两个接口，用来实现扩展）

1. NamespaceHandler: 注册一堆 BeanDefinitionParser，利用他们来进行解析

2. BeanDefinitionParser:用于解析每个 element 的内容
3. Spring 默认会加载 jar 包下的 META-INF/spring.handlers 文件寻找对应的 NamespaceHandler。以下是 Dubbo-config 模块下的 dubbo-config-spring

```java
public class DubboNamespaceHandler extends NamespaceHandlerSupport {
    public DubboNamespaceHandler() {
    }

    public void init() {
        this.registerBeanDefinitionParser("application", new DubboBeanDefinitionParser(ApplicationConfig.class, true));
        this.registerBeanDefinitionParser("module", new DubboBeanDefinitionParser(ModuleConfig.class, true));
        this.registerBeanDefinitionParser("registry", new DubboBeanDefinitionParser(RegistryConfig.class, true));
        this.registerBeanDefinitionParser("monitor", new DubboBeanDefinitionParser(MonitorConfig.class, true));
        this.registerBeanDefinitionParser("provider", new DubboBeanDefinitionParser(ProviderConfig.class, true));
        this.registerBeanDefinitionParser("consumer", new DubboBeanDefinitionParser(ConsumerConfig.class, true));
        this.registerBeanDefinitionParser("protocol", new DubboBeanDefinitionParser(ProtocolConfig.class, true));
        this.registerBeanDefinitionParser("service", new DubboBeanDefinitionParser(ServiceBean.class, true));
        this.registerBeanDefinitionParser("reference", new DubboBeanDefinitionParser(ReferenceBean.class, false));
        this.registerBeanDefinitionParser("annotation", new DubboBeanDefinitionParser(AnnotationBean.class, true));
    }
    static {
        Version.checkDuplicate(DubboNamespaceHandler.class);
    }
}
```

​			1. Dubbo 的接入实现Dubbo 中 spring 扩展就是使用 spring 的自定义类型，所以同样也有NamespaceHandler、BeanDefinitionParser。而 NamespaceHandler 是DubboNamespaceHandler。

​			2. BeanDefinitionParser 全部都使用了 DubboBeanDefinitionParser，如果我们向看<dubbo:service>的配置，就直接看 DubboBeanDefinitionParser中

​            3.这个里面主要做了一件事，把不同的配置分别转化成spring容器中的bean

```xml
对象
application 对应 ApplicationConfig
registry 对应 RegistryConfig
monitor 对应 MonitorConfig
provider 对应 ProviderConfig
consumer 对应 ConsumerConfig
…
```

​				为了在 spring 启动的时候，也相应的启动 provider 发布服务注册服务的过程，而同时为了让客户端在启动的时候自动订阅发现服务，加入了两个beanServiceBean、ReferenceBean。分别继承了 ServiceConfig 和 ReferenceConfig,同 时 还 分 别 实 现 了 InitializingBean 、 DisposableBean,ApplicationContextAware, ApplicationListener, BeanNameAware。

##### 6.1.1 InitializingBean

​			为 bean 提供了初始化方法的方式，它只包括afterPropertiesSet 方法，凡是继承该接口的类，在初始化 bean 的时候会执行该方法。

##### 6.1.2 DisposableBean

​			bean 被销毁的时候，spring 容器会自动执行 destory 方法，比如释放资源

##### 6.1.3 ApplicationContextAware

​			实现了这个接口的 bean，当 spring 容器初始化的时候，会自动的将 ApplicationContext 注入进来

##### 6.1.4 ApplicationListener

​			ApplicationEvent 事件监听，spring 容器启动后会发一个事件通知

#####  6.1.5 BeanNameAware

​			获得自身初始化时，本身的 bean 的 id 属性

**那么基本的实现思路**：

​			1. 利用 spring 的解析收集 xml 中的配置信息，然后把这些配置信息存储到 serviceConfig 中

​			2. 调用 ServiceConfig 的 export 方法来进行服务的发布和注册

#### 6.2 服务的发布过程

serviceBean 是服务发布的切入点，通过 afterPropertiesSet 方法，调用export()方法进行发布。

代码如下：

```java
public void afterPropertiesSet() throws Exception {
        
        if (! isDelay()) {
            export();
        }
    }
```

export 为父类 ServiceConfig 中的方法，所以跳转到 SeviceConfig 类中的export 方法delay 的使用

```java
public synchronized void export() {
        if (delay != null && delay > 0) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(delay);
                    } catch (Throwable e) {
                    }
                    doExport();
                }
            });
            thread.setDaemon(true);
            thread.setName("DelayExportServiceThread");
            thread.start();
        } else {
            doExport();
        }
    }
```

**说明：**

​			1. export 是 synchronized 修饰的方法。也就是说暴露的过程是原子操作，正常情况下不会出现锁竞争的问题，毕竟初始化过程大多数情况下都是单一线程操作，这里联想到了 spring 的初始化流程，也进行了加锁操作这里也给我们平时设计一个不错的启示：**初始化流程的性能调优优先级应该放的比较低，但是安全的优先级应该放的比较高！**

​			2.继续看 doExport()方法。同样是一堆初始化代码export 的过程继续看 doExport()，最终会调用到 doExportUrls()中：

```java
 private void doExportUrls() {
        List<URL> registryURLs = loadRegistries(true);//是不是获得注册中心的配置
        for (ProtocolConfig protocolConfig : protocols) { //是不是支持多协议发布
            doExportUrlsFor1Protocol(protocolConfig, registryURLs);
        }
    }
```

<dubbo:protocol name="dubbo" port="20888" id="dubbo" />

注意：protocols 也是根据配置装配出来的。接下来让我们进入 doExportUrlsFor1Protocol 方法看看 dubbo 具体是怎么样将服务暴露出去的

最终实现逻辑：

```java
private void doExportUrlsFor1Protocol(ProtocolConfig protocolConfig, List<URL> registryURLs) {
      
                if (registryURLs != null && registryURLs.size() > 0
                        && url.getParameter("register", true)) {
                    for (URL registryURL : registryURLs) {//
                       
                        //通过proxyFactory来获取Invoker对象
                        Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(Constants.EXPORT_KEY, url.toFullString()));
                        //注册服务
                        Exporter<?> exporter = protocol.export(invoker);
                        //将exporter添加到list中
                        exporters.add(exporter);
                    }
                } else {
                    Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, url);
                    Exporter<?> exporter = protocol.export(invoker);
                    exporters.add(exporter);
                }
            }
        }
```

dubbo 的工作原理: doExportUrlsFor1Protocol 方法，先创建两个 URL，分别如下

dubbo://192.168.xx.63:20888/com.gupaoedu.IGHello;
registry://192.168.xx ;

​		在上面这段代码中可以看到 Dubbo 的比较核心的抽象：Invoker， Invoker是一个代理类，从 ProxyFactory 中生成。这个地方可以做一个小结:

1. Invoker - 执行具体的远程调用
2. Protocol – 服务地址的发布和订阅
3. Exporter – 暴露服务或取消暴露

##### 6.2.1 protocol.export(invoker)

​			protocol 这个地方，其实并不是直接调用 DubboProtocol 协议的 export,大家跟我看看 protocol 这个属性是在哪里实例化的？以及实例化的代码是什么？

```java
private static final Protocol protocol = ExtensionLoader.getExtensionLoader (Protocol.class).getAdaptiveExtension(); 
```

​			实际上这个 Protocol 得到的应该是一个 Protocol$Adaptive。一个自适应的适配器。这个时候，通过 protocol.export(invoker),实际上调用的应该是Protocol$Adaptive 这个动态类的 export 方法。

```java
import com.alibaba.dubbo.common.extension.ExtensionLoader;

public class Protocol$Adaptive implements com.alibaba.dubbo.rpc.Protocol {
    public void destroy() {
        throw new UnsupportedOperationException("method public abstract void com.alibaba.dubbo.rpc.Protocol.destroy() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
    }

    public int getDefaultPort() {
        throw new UnsupportedOperationException("method public abstract int com.alibaba.dubbo.rpc.Protocol.getDefaultPort() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
    }

    public com.alibaba.dubbo.rpc.Invoker refer(Class arg0, URL arg1) throws RpcException {
        if (arg1 == null) throw new IllegalArgumentException("url == null");
        URL url = arg1;
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(Protocol) name from url(" + url.toString() + ") use keys([protocol])");
        Protocol extension = (Protocol) ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);
        return extension.refer(arg0, arg1);
    }

    public Exporter export(com.alibaba.dubbo.rpc.Invoker arg0) throws com.alibaba.dubbo.rpc.RpcException {
        if (arg0 == null) throw new IllegalArgumentException("Invoker argument == null");
        if (arg0.getUrl() == null)
            throw new IllegalArgumentException("Invoker argument getUrl() == null");
        URL url = arg0.getUrl();
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(Protocol) name from url(" + url.toString() + ") use keys([protocol])");
        Protocol extension = (Protocol) ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);
        return extension.export(arg0);
    }
}
```

上面这段代码做两个事情：

1. 从 url 中获得 protocol 的协议地址，如果 protocol 为空，表示已 dubbo协议发布服务，否则根据配置的协议类型来发布服务。 ?
2. 调 用ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);



**ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);**

​		这个方法的主要作用是用来获取 ExtensionLoader 实例代表的扩展的指定实现。已扩展实现的名字作为参数

```java
public T getExtension(String name) {
		if (name == null || name.length() == 0)
		    throw new IllegalArgumentException("Extension name == null");
		if ("true".equals(name)) {
		    return getDefaultExtension();
		}
		Holder<Object> holder = cachedInstances.get(name);
		if (holder == null) {
		    cachedInstances.putIfAbsent(name, new Holder<Object>());
		    holder = cachedInstances.get(name);
		}
		Object instance = holder.get();
		if (instance == null) {
		    synchronized (holder) {
	            instance = holder.get();
	            if (instance == null) {
	                instance = createExtension(name);
	                holder.set(instance);
	            }
	        }
		}
		return (T) instance;
	}
```

createExtension :这个方法主要做 4 个事情

		  1. 根据 name 获取对应的 class
   		  2. 根据 class 创建一个实例

   3. 对获取的实例进行依赖注入
   4. 对实例进行包装，分别调用带 Protocol 参数的构造函数创建实例，然后进行依赖注入。
- 在 dubbo-rpc-api 的 resources 路 径 下 ， 找 到com.alibaba.dubbo.rcp.Protocol 文件中有存在 filter/listener
- 遍历 cachedWrapperClass 对 DubboProtocol 进行包装，会通过ProtocolFilterWrapper、ProtocolListenerWrapper 包装

```java
private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);//"dubbo"  clazz=DubboProtocol
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, (T) clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (wrapperClasses != null && wrapperClasses.size() > 0) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    // 对实例进行包装，分别调用带 Protocol 参数的构造函数创建实例，然后进行依赖注入。
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }
```

##### 6.2.2 getExtensionClasses

​			就是加载扩展点实现类了。然后调用 loadExtensionClasses，去对应文件下去加载指定的扩展点

```java
//加载扩展点的实现类
	private Map<String, Class<?>> getExtensionClasses() {

        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
	}
```

**总结**：

- ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);

  这 段 代 码 中 ，ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName); 

  ​		当 extName 为 registry 的时候，直接可以在扩展点中找到相应的实现扩展点[/dubbo-registry-api/src/main/resources/META-INF/dubbo/internal/com.alibaba.dubbo.rpc.Protocol] 配置如下

```xml
registry=com.alibaba.dubbo.registry.integration.RegistryProtocol
```

所以，我们可以定位到 RegistryProtocol这个类中的export 方法

```java
 public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
        //export invoker
        final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);
        //registry provider
        final Registry registry = getRegistry(originInvoker);
        //得到需要注册到zk上的协议地址，也就是dubbo://
        final URL registedProviderUrl = getRegistedProviderUrl(originInvoker);
        registry.register(registedProviderUrl);
        // 订阅override数据
        // FIXME 提供者订阅时，会影响同一JVM即暴露服务，又引用同一服务的的场景，因为subscribed以服务名为缓存的key，导致订阅信息覆盖。
        final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registedProviderUrl);
        final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl);
        overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
        registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);
        //保证每次export都返回一个新的exporter实例
        return new Exporter<T>() {
            public Invoker<T> getInvoker() {
                return exporter.getInvoker();
            }
            public void unexport() {
            	try {
            		exporter.unexport();
            	} catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	registry.unregister(registedProviderUrl);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	overrideListeners.remove(overrideSubscribeUrl);
                	registry.unsubscribe(overrideSubscribeUrl, overrideSubscribeListener);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
            }
        };
    }
```



##### 6.2.3 doLocalExport

​	本地先启动监听服务

**protocol 代码是怎么赋值的呢？**

PS：有一个 injectExtension 方法，针对已经加载的扩展点中的扩展点属性进行依赖注入。

```java
private Protocol protocol;
	public void setProtocol(Protocol protocol) {
	this.protocol = protocol;
}
```

```java
private <T> ExporterChangeableWrapper<T>  doLocalExport(final Invoker<T> originInvoker){
        String key = getCacheKey(originInvoker);
        ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
        if (exporter == null) {
            synchronized (bounds) {
                exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
                if (exporter == null) {
                    final Invoker<?> invokerDelegete = new InvokerDelegete<T>(originInvoker, getProviderUrl(originInvoker));
                    exporter = new ExporterChangeableWrapper<T>((Exporter<T>)protocol.export(invokerDelegete), originInvoker);
                    bounds.put(key, exporter);
                }
            }
        }
        return (ExporterChangeableWrapper<T>) exporter;
    }
```

**protocol 代码是怎么赋值的呢？**

PS：有一个 injectExtension 方法，针对已经加载的扩展点中的扩展点属性进行依赖注入。

```java
private Protocol protocol;
	public void setProtocol(Protocol protocol) {
	this.protocol = protocol;
}
```



##### 6.2.4 protocol.export

 		protocol 是一个自适应扩展点，Protocol$Adaptive，然后调用这个自适应扩展点中的 export 方法，这个时候传入的协议地址应该是dubbo://127.0.0.1/xxxx…  ？？？

​		这里并不是获得一个单纯的 DubboProtocol 扩展点，而是会通过 Wrapper对 Protocol 进 行 装 饰 ， 装 饰 器 分 别 为 : ProtocolFilterWrapper/ProtocolListenerWrapper; 

​		至于 MockProtocol 为什么不在装饰器里面呢？大家再回想一下我们在看 ExtensionLoader.loadFile 这段代码的时候，有一个判断，装饰器必须要具备一个带有 Protocol 的构造方法，如下

```java
public class ProtocolFilterWrapper implements Protocol {

    private final Protocol protocol;

    public ProtocolFilterWrapper(Protocol protocol){
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }
```

​		截止到这里，我们已经知道，Protocol$Adaptive 里面的 export 方法，会调用 ProtocolFilterWrapper 以及 ProtocolListenerWrapper 类的方法

**这两个装饰器是用来干嘛的呢？**

​		分析 ProtocolFilterWrapper 和 ProtocolListenerWrapper

##### 6.2.5 ProtocolFilterWrapper

​		这个类非常重要，dubbo 机制里面日志记录、超时等等功能都是在这一部分实现的

这个类有 3 个特点，
	第一它有一个参数为 Protocol protocol 的构造函数；
	第二，它实现了 Protocol 接口；
	第三，它使用责任链模式，对 export 和 refer 函数进行了封装；部分代码
如下

```java
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
            return protocol.export(invoker);
        }
        return protocol.export(buildInvokerChain(invoker, Constants.SERVICE_FILTER_KEY, Constants.PROVIDER));
    }

    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
            return protocol.refer(type, url);
        }
        return buildInvokerChain(protocol.refer(type, url), Constants.REFERENCE_FILTER_KEY, Constants.CONSUMER);
    }

    public void destroy() {
        protocol.destroy();
    }

    private static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key, String group) {
        Invoker<T> last = invoker;
        List<Filter> filters = ExtensionLoader.getExtensionLoader(Filter.class).getActivateExtension(invoker.getUrl(), key, group);
        if (filters.size() > 0) {
            for (int i = filters.size() - 1; i >= 0; i --) {
                final Filter filter = filters.get(i);
                final Invoker<T> next = last;
                last = new Invoker<T>() {

                    public Class<T> getInterface() {
                        return invoker.getInterface();
                    }

                    public URL getUrl() {
                        return invoker.getUrl();
                    }

                    public boolean isAvailable() {
                        return invoker.isAvailable();
                    }

                    public Result invoke(Invocation invocation) throws RpcException {
                        return filter.invoke(next, invocation);
                    }

                    public void destroy() {
                        invoker.destroy();
                    }

                    @Override
                    public String toString() {
                        return invoker.toString();
                    }
                };
            }
        }
        return last;
    }
```

​			如 下 文 件 ： /dubbo-rpc-api/src/main/resources/META-INF/dubbo/internal/com.alibaba.dubbo.rpc.Filter其实就是对 Invoker，通过如下的 Filter 组装成一个责任链

```xml
echo=com.alibaba.dubbo.rpc.filter.EchoFilter
generic=com.alibaba.dubbo.rpc.filter.GenericFilter
genericimpl=com.alibaba.dubbo.rpc.filter.GenericImplFilter
token=com.alibaba.dubbo.rpc.filter.TokenFilter
accesslog=com.alibaba.dubbo.rpc.filter.AccessLogFilter
activelimit=com.alibaba.dubbo.rpc.filter.ActiveLimitFilter
classloader=com.alibaba.dubbo.rpc.filter.ClassLoaderFilter
context=com.alibaba.dubbo.rpc.filter.ContextFilter
consumercontext=com.alibaba.dubbo.rpc.filter.ConsumerContextFilt
er
exception=com.alibaba.dubbo.rpc.filter.ExceptionFilter
executelimit=com.alibaba.dubbo.rpc.filter.ExecuteLimitFilter
deprecated=com.alibaba.dubbo.rpc.filter.DeprecatedFilter
compatible=com.alibaba.dubbo.rpc.filter.CompatibleFilter
timeout=com.alibaba.dubbo.rpc.filter.TimeoutFilter
```

​		这其中涉及到很多功能，包括权限验证、异常、超时等等，当然可以预计计算调用时间等等应该也是在这其中的某个类实现的；这里我们可以看到 export 和 refer 过程都会被 filter 过滤

##### 6.2.6 ProtocolListenerWrapper

​			在这里我们可以看到 export 和 refer 分别对应了不同的 Wrapper；export是对应的 ListenerExporterWrapper。这块暂时先不去分析，因为这个地方并没有提供实现类。



##### 6.2.7 DubboProtocol.export

​		通过上面的代码分析完以后，最终我们能够定位到 DubboProtocol.export方法。我们看一下 dubboProtocol 的 export 方法：openServer(url）export

```java
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();
        
        // export service.
        String key = serviceKey(url);
        DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, exporterMap);
        exporterMap.put(key, exporter);

        //export an stub service for dispaching event
        Boolean isStubSupportEvent = url.getParameter(Constants.STUB_EVENT_KEY,Constants.DEFAULT_STUB_EVENT);
        Boolean isCallbackservice = url.getParameter(Constants.IS_CALLBACK_SERVICE, false);
        if (isStubSupportEvent && !isCallbackservice){
            String stubServiceMethods = url.getParameter(Constants.STUB_EVENT_METHODS_KEY);
            if (stubServiceMethods == null || stubServiceMethods.length() == 0 ){
                if (logger.isWarnEnabled()){
                    logger.warn(new IllegalStateException("consumer [" +url.getParameter(Constants.INTERFACE_KEY) +
                            "], has set stubproxy support event ,but no stub methods founded."));
                }
            } else {
                stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods);
            }
        }

        openServer(url);
        
        return exporter;
    }
```



##### 6.2.8 openServer

开启服务

```java
private void openServer(URL url) {
        // find server.
        String key = url.getAddress();
        //client 也可以暴露一个只有server可以调用的服务。
        boolean isServer = url.getParameter(Constants.IS_SERVER_KEY,true);
        if (isServer) {
        	ExchangeServer server = serverMap.get(key);
        	if (server == null) {
        		serverMap.put(key, createServer(url));
        	} else {
        		//server支持reset,配合override功能使用
        		server.reset(url);
        	}
        }
    }
```

##### 6.2.9 createServer

创建服务,开启心跳检测，默认使用 netty。组装 url

```java
private ExchangeServer createServer(URL url) {
        //默认开启server关闭时发送readonly事件
        url = url.addParameterIfAbsent(Constants.CHANNEL_READONLYEVENT_SENT_KEY, Boolean.TRUE.toString());
        //默认开启heartbeat
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
        String str = url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_SERVER);

        if (str != null && str.length() > 0 && ! ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str))
            throw new RpcException("Unsupported server type: " + str + ", url: " + url);

        url = url.addParameter(Constants.CODEC_KEY, Version.isCompatibleVersion() ? COMPATIBLE_CODEC_NAME : DubboCodec.NAME);
        ExchangeServer server;
        try {
            server = Exchangers.bind(url, requestHandler);
        } catch (RemotingException e) {
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
        }
        str = url.getParameter(Constants.CLIENT_KEY);
        if (str != null && str.length() > 0) {
            Set<String> supportedTypes = ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions();
            if (!supportedTypes.contains(str)) {
                throw new RpcException("Unsupported client type: " + str);
            }
        }
        return server;
    }
```

###### 6.2.9.1 Exchangers. bind

```java
public static ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        url = url.addParameterIfAbsent(Constants.CODEC_KEY, "exchange");
        return getExchanger(url).bind(url, handler);
    }
```

###### 6.2.9.2 GETEXCHANGER

通过 ExtensionLoader 获得指定的扩展点，type 默认为 header

```java
public static Exchanger getExchanger(URL url) {
        String type = url.getParameter(Constants.EXCHANGER_KEY, Constants.DEFAULT_EXCHANGER);
        return getExchanger(type);
    }
```

###### 6.2.9.3 HeaderExchanger.bind

调用 headerExchanger 的 bind 方法

```java
public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
    }
```

###### 6.2.9.4 Transporters.bind



###### 6.2.9.5 NettyTransport.bind

通过 NettyTranport 创建基于 Netty 的 server 服务

```java
public Server bind(URL url, ChannelHandler listener) throws
	RemotingException {
	return new NettyServer(url, listener);
}
```

### 七、 服务注册的过程

#### 7.1 RegistryProtocol.export

```java
public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
        //export invoker
        final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);
        //registry provider
        final Registry registry = getRegistry(originInvoker);
        //得到需要注册到zk上的协议地址，也就是dubbo://
        final URL registedProviderUrl = getRegistedProviderUrl(originInvoker);
        registry.register(registedProviderUrl);
        // 订阅override数据
        // FIXME 提供者订阅时，会影响同一JVM即暴露服务，又引用同一服务的的场景，因为subscribed以服务名为缓存的key，导致订阅信息覆盖。
        final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registedProviderUrl);
        final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl);
        overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
        registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);
        //保证每次export都返回一个新的exporter实例
        return new Exporter<T>() {
            public Invoker<T> getInvoker() {
                return exporter.getInvoker();
            }
            public void unexport() {
            	try {
            		exporter.unexport();
            	} catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	registry.unregister(registedProviderUrl);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	overrideListeners.remove(overrideSubscribeUrl);
                	registry.unsubscribe(overrideSubscribeUrl, overrideSubscribeListener);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
            }
        };
    }
```

#### 7.2 getRegistry

​	通过前面这段代码的分析，其实就是把 registry 的协议头改成服务提供者配置的协议地址，也就是我们配置的<dubbo:registry address=”zookeeper://192.168.11.156:2181”/>然后 registryFactory.getRegistry 的目的，就是通过协议地址匹配到对应的注册中心

那 registryFactory 是一个什么样的对象呢？

```java
private RegistryFactory registryFactory;
    
    public void setRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
 }
```

 RegistryFactory 这个类的定义,是一个扩展点注意这里面的一个方法上，有一个@Adaptive 的注解，说
明什么？ 这个是一个自适应扩展点。按照我们之前看过代码，自适应扩展点加在方法层面上，表示会动态生成一个自适应的适配器。所以这个自适应适配器应该是 RegistryFactory$Adaptive

```java
public class RegistryFactory$Adaptive implements RegistryFactory {
    public Registry getRegistry(com.alibaba.dubbo.common.URL arg0) {
        String extName = (url.getProtocol() == null ? "dubbo" :url.getProtocol());
       RegistryFactory extension = (RegistryFactory)ExtensionLoader.getExtensionLoader(RegistryFactory.class).
                                getExtension(extName);
        return extension.getRegistry(arg0);
    }
}
```

##### 7.2.1  RegistryFactory$Adaptive

​	我们拿到这个动态生成的自适应扩展点，看看这段代码里面的实现

- 从 url 中拿到协议头信息，这个时候的协议头是 zookeeper://

-  过ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtension(“zookeeper”)去获得一个指定的扩展点，而这个扩展点的配置在dubbo-registry-zookeeper/resources/META-INF/dubbo/internal/com.alibaba.dubbo.registry.RegistryFactory。得到一个 ZookeeperRegistryFactory

​		这个方法是 invoker 的地址获取 registry 实例

```java
/**
     * 根据invoker的地址获取registry实例；
     * 实际上就是
     * @param originInvoker
     * @return
     */
    private Registry getRegistry(final Invoker<?> originInvoker){
        URL registryUrl = originInvoker.getUrl(); //registry://
        if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
            registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
        }//zookeeper://
        return registryFactory.getRegistry(registryUrl);
    }
```

##### 7.2.2 ZookeeperRegistryFactory	

这个方法是 invoker 的地址获取 registry 实例

```java
/**
     * 根据invoker的地址获取registry实例；
     * 实际上就是
     * @param originInvoker
     * @return
     */
    private Registry getRegistry(final Invoker<?> originInvoker){
        URL registryUrl = originInvoker.getUrl(); //registry://
        if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
            registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
        }//zookeeper://
        return registryFactory.getRegistry(registryUrl);
    }
```



##### 7.2.3 createRegistry

​	创建一个注册中心，这个是一个抽象方法，具体的实现在对应的子类实例中实现的，在 ZookeeperRegistryFactory 中

```java
public Registry createRegistry(URL url) {
		return new ZookeeperRegistry(url, zookeeperTransporter);
}
通过 zkClient，获得一个 zookeeper 的连接实例
public ZookeeperRegistry(URL url, ZookeeperTransporterzookeeperTransporter) {
	super(url);
	if (url.isAnyHost()) {
		throw new IllegalStateException("registry address == null");
	}
		String group = url.getParameter(Constants. GROUP_KEY ,DEFAULT_ROOT );
	if (! group.startsWith(Constants. PATH_SEPARATOR )) {
		group = Constants. PATH_SEPARATOR + group;
	}
	this.root = group; //设置根节点
		zkClient = zookeeperTransporter.connect(url);//建立连接
		zkClient.addStateListener(new StateListener() {
	public void stateChanged(int state) {
		if (state == RECONNECTED ) {
			try {
				recover();
			} catch (Exception e) {
				logger .error(e.getMessage(), e);
			}
		}
		}
	});
}
```

​		代码分析到这里，我们对于 getRegistry 得出了一个结论，根据当前注册中心的配置信息，获得一个匹配的注册中心，也就是 Zookeeper

#### 7.3 Registryregistry.register(registedProviderUrl)

​		继续往下分析，会调用 registry.register 去讲 dubbo://的协议地址注册到zookeeper 上这个方法会调用 FailbackRegistry 类中的 register. 为什么呢？

​		因为ZookeeperRegistry 这个类中并没有 register 这个方法，但是他的父类FailbackRegistry中存在register方法，而这个类又重写了AbstractRegistry类中的 register 方法。所以我们可以直接定位大 FailbackRegistry 这个类中的 register 方法中

##### 7.3.1 FailbackRegistry.register

1. FailbackRegistry，从名字上来看，是一个失败重试机制
2. 调用父类的 register 方法，讲当前 url 添加到缓存集合中
3. 调用 doRegister 方法，这个方法很明显，是一个抽象方法，会由ZookeeperRegistry 子类实现。



```java
@Override
public void register(URL url) {
		super.register(url);
		failedRegistered.remove(url);
		failedUnregistered.remove(url);
		try {
		// 向服务器端发送注册请求
			doRegister(url);
		} catch (Exception e) {
			Throwable t = e;
			// 如果开启了启动时检测，则直接抛出异常
		boolean check =getUrl().getParameter(Constants. CHECK_KEY , true)&& 					url.getParameter(Constants. CHECK_KEY , true)&& !
			Constants. CONSUMER_PROTOCOL .equals(url.getProtocol());
		boolean skipFailback = t instanceof
		SkipFailbackWrapperException;
		if (check || skipFailback) {
			if(skipFailback) {
				t = t.getCause();
			}
		throw new IllegalStateException("Failed to register " +
		url + " to registry " + getUrl().getAddress() + ", cause: " +t.getMessage(), t);
		} else {
			logger.error("Failed to register " + url + ", waiting for
			retry, cause: " + t.getMessage(), t);
		}
		// 将失败的注册请求记录到失败列表，定时重试
		failedRegistered.add(url);
	}
}
```

##### 7.3.2 ZookeeperRegistry.doRegister

```java
protected void doRegister(URL url) {
        try {
        	zkClient.create(toUrlPath(url), url.getParameter(Constants.DYNAMIC_KEY, true));
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
```

​		RegistryProtocol.export 这个方法中后续的代码就不用再分析了。就是去对服务提供端去注册一个 zookeeper 监听，当监听发生变化的时候，服务端做相应的处理。

### 八、消费端启动初始化过程

#### 8.1  启动入口

消费端的代码解析是从下面这段代码开始的

```java
<dubbo:reference id="xxxService" interface="xxx.xxx.Service"/>
```

```
ReferenceBean(afterPropertiesSet) ->getObject() ->get()->init()->createProxy  最终会获得一个代理对象。
```

#### 8.2 createProxy 第375行

初始化创建客户端的代理对象，接下来主要关注如何创建这个代理

```java
private void init() {
	   
        //attributes通过系统context进行存储.
        StaticContext.getSystemContext().putAll(attributes);
        ref = createProxy(map);
    }
```

判断是否为集群方式

```java
if (urls.size() == 1) {
                invoker = refprotocol.refer(interfaceClass, urls.get(0));
            } else {
                List<Invoker<?>> invokers = new ArrayList<Invoker<?>>();
                URL registryURL = null;
                for (URL url : urls) {
                    invokers.add(refprotocol.refer(interfaceClass, url));
                    if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
                        registryURL = url; // 用了最后一个registry url
                    }
                }
                if (registryURL != null) { // 有 注册中心协议的URL
                    // 对有注册中心的Cluster 只用 AvailableCluster
                    URL u = registryURL.addParameter(Constants.CLUSTER_KEY, AvailableCluster.NAME); 
                    invoker = cluster.join(new StaticDirectory(u, invokers));
                }  else { // 不是 注册中心的URL
                    invoker = cluster.join(new StaticDirectory(invokers));
                }
            }
```

#### 8.3  refprotocol.refer

```java
private static final Protocol refprotocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();

```

refprotocol这个对象，定义的代码如下，是一个自适应扩展点，得到的是Protocol$Adaptive。

​			直接找到Protocol$Adaptive代码中的refer代码块如下这段代码中，根据当前的协议url，得到一个指定的扩展点，传递进来的参数中，协议地址为registry://，所以，我们可以直接定位到RegistryProtocol.refer代码

```java
  public com.alibaba.dubbo.rpc.Invoker refer(Class arg0,URL arg1) throws RpcException {
        if (arg1 == null) throw new IllegalArgumentException("url == null");
        URL url = arg1;
        String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url(" + url.toString() + ") use keys([protocol])");
        Protocol extension = (com.alibaba.dubbo.rpc.Protocol) ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(extName);
        return extension.refer(arg0, arg1);
    }
```

#### 8.4 RegistryProtocol.refer

```java
public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        url = url.setProtocol(url.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_REGISTRY)).removeParameter(Constants.REGISTRY_KEY);
        Registry registry = registryFactory.getRegistry(url);
        if (RegistryService.class.equals(type)) {
        	return proxyFactory.getInvoker((T) registry, type, url);
        }

        // group="a,b" or group="*"
        Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(Constants.REFER_KEY));
        String group = qs.get(Constants.GROUP_KEY);
        if (group != null && group.length() > 0 ) {
            if ( ( Constants.COMMA_SPLIT_PATTERN.split( group ) ).length > 1
                    || "*".equals( group ) ) {
                return doRefer( getMergeableCluster(), registry, type, url );
            }
        }
        return doRefer(cluster, registry, type, url);
    }
```

这个方法里面的代码，基本上都能看懂
1.    根据根据url获得注册中心，这个registry是zookeeperRegistry
2.   调用doRefer，按方法，传递了几个参数， 其中有一个culster参数，这个需要注意下

#### 8.5 cluster

doRefer方法中有一个参数是cluster,我们找到它的定义代码如下，。又是一个自动注入的扩展点。

```java
 private Cluster cluster;
    
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
```

源类模式：默认加载扩展点，FailoverCluster

```java
@SPI(FailoverCluster.NAME)
public interface Cluster {

    /**
     * Merge the directory invokers to a virtual invoker.
     * 
     * @param <T>
     * @param directory
     * @return cluster invoker
     * @throws RpcException
     */
    @Adaptive
    <T> Invoker<T> join(Directory<T> directory) throws RpcException;

}
```

​		从下面的代码可以看出，这个不仅仅是一个扩展点，而且方法层面上，还有一个@Adaptive，表示会动态生成一个自适应适配器Cluster$Adaptive

#### 8.6 Cluster$Adaptive

获取到Cluster$Adaptive这个适配器，代码如下：

```java
public class Cluster$Adaptive implements Cluster {
    public Invoker join(Directory arg0) throws RpcException {
        if (arg0 == null)
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.cluster.Directory argument == null");
        if (arg0.getUrl() == null)
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.cluster.Directory argument getUrl() == null");
        URL url = arg0.getUrl();
        String extName = url.getParameter("cluster", "failover"); //failover
        if (extName == null)
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.cluster.Cluster) name from url(" + url.toString() + ") use keys([cluster])");
        Cluster extension = (Cluster) ExtensionLoader.getExtensionLoader(Cluster.class).getExtension(extName);
        return extension.join(arg0);
    }
}
```

​			注意：这里的Cluster$Adaptive也并不单纯，大家还记得在讲扩展点的时候有一个扩展点装饰器吗？如果这个扩展点存在一个构造函数，并且构造函数就是扩展接口本身，那么这个扩展点就会这个wrapper装饰，而Cluster被装饰的是：MockClusterWrapper

```
MockClusterWrapper(FailoverCluster())
```

#### 8.7 RegistryProtocol.doRefer

```java
private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);
        URL subscribeUrl = new URL(Constants.CONSUMER_PROTOCOL, NetUtils.getLocalHost(), 0, type.getName(), directory.getUrl().getParameters());
        if (! Constants.ANY_VALUE.equals(url.getServiceInterface())
                && url.getParameter(Constants.REGISTER_KEY, true)) {
            registry.register(subscribeUrl.addParameters(Constants.CATEGORY_KEY, Constants.CONSUMERS_CATEGORY,
                    Constants.CHECK_KEY, String.valueOf(false)));
        }
    //订阅3个地址
        directory.subscribe(subscribeUrl.addParameter(Constants.CATEGORY_KEY, 
                Constants.PROVIDERS_CATEGORY 
                + "," + Constants.CONFIGURATORS_CATEGORY 
                + "," + Constants.ROUTERS_CATEGORY));
        return cluster.join(directory);
    }
```

基于注册中心动态发现服务提供者）
1.    将consumer://协议地址注册到注册中心
2.   订阅zookeeper地址的变化
3.   调用cluster.join()方法

#### 8.8 cluster.join

​		由前面的Cluster$Adaptive这个类中的join方法的分析，得知cluster.join会调用MockClusterWrapper.join方法，然后再调用FailoverCluster.join方法。

##### 8.8.1 MockClusterWrapper.join

​		这个意思很明显了。也就是我们上节课讲过的mock容错机制，如果出现异常情况，会调用MockClusterInvoker，否则，调用FailoverClusterInvoker.

```java
public class MockClusterWrapper implements Cluster {

	private Cluster cluster;

	public MockClusterWrapper(Cluster cluster) {
		this.cluster = cluster;
	}

	public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
		return new MockClusterInvoker<T>(directory,
				this.cluster.join(directory));
	}

}
```

**小结**

​		refprotocol.ref，这个方法，会返回一个MockClusterInvoker(FailoverClusterInvoker)。这里面一定还有疑问，我们先把主线走完，再回过头看看什么是cluster、什么是directory

#### 8.9 proxyFactory.getProxy(invoker);

```java
private T createProxy(Map<String, String> map) {
		
            if (urls.size() == 1) {
                invoker = refprotocol.refer(interfaceClass, urls.get(0));
            } else {
                List<Invoker<?>> invokers = new ArrayList<Invoker<?>>();
                URL registryURL = null;
                for (URL url : urls) {
                    invokers.add(refprotocol.refer(interfaceClass, url));
                    if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
                        registryURL = url; // 用了最后一个registry url
                    }
                }
                if (registryURL != null) { // 有 注册中心协议的URL
                    // 对有注册中心的Cluster 只用 AvailableCluster
                    URL u = registryURL.addParameter(Constants.CLUSTER_KEY, AvailableCluster.NAME); 
                    invoker = cluster.join(new StaticDirectory(u, invokers));
                }  else { // 不是 注册中心的URL
                    invoker = cluster.join(new StaticDirectory(invokers));
                }
            }
        }
        // 创建服务代理
        return (T) proxyFactory.getProxy(invoker);
    }
```

return (T) proxyFactory.getProxy(invoker);

​		在createProxy方法的最后一行，调用proxyFactory.getProxy(invoker).把前面生成的invoker对象作为参数，再通过proxyFactory工厂去获得一个代理对象。

- 其实前面在分析服务发布的时候，基本分析过了，所以再看这段代码，应该会很熟悉

```java
public java.lang.Object getProxy(com.alibaba.dubbo.rpc.Invoker arg0) throws com.alibaba.dubbo.rpc.RpcException {
        if (arg0 == null)
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument == null");
        if (arg0.getUrl() == null)
            throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument getUrl() == null");com.alibaba.dubbo.common.URL url = arg0.getUrl();
        String extName = url.getParameter("proxy", "javassist");
        if(extName == null)
            throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.ProxyFactory) name from url(" + url.toString() + ") use keys([proxy])");
        ProxyFactory extension = (ProxyFactory)ExtensionLoader.getExtensionLoader(ProxyFactory.class).getExtension(extName);
        return extension.getProxy(arg0);
    }
```

ProxyFactory， 会生成一个动态的自适应适配器。ProxyFactory$Adaptive，然后调用这个适配器中的getProxy方法 ,通过javassist实现的一个动态代理，我们来看看JavassistProxyFactory.getProxy

#### 8.10 JavassistProxyFactory.getProxy

通过javasssist动态字节码生成动态代理类

```java
public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
    return (T) Proxy.getProxy(interfaces).newInstance(newInvokerInvocationHandler(invoker));
}
```

#### 8.11 proxy.getProxy(interfaces)

在Proxy.getProxy这个类的如下代码中添加断点，在debug下可以看到动态字节码如下

![1601563058889](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601563058889.png)

```java
public java.lang.String sayHello(java.lang.String arg0){
  Object[] args = new Object[1]; 
  args[0] = ($w)$1; 
  Object ret = handler.invoke(this, methods[0], args); 
  return (java.lang.String)ret;
}
```

上面红色部分代码的handler，就是在JavassistProxyFactory.getProxy中。传递的new InvokerInvocationHandler(invoker)

### 九、消费端和服务端建立连接

客户端和服务端建立NIO连接的时机是什么时候?

​		际上，建立连接的过程在消费端初始化的时候就建立好的，代码在RegistryProtocol.doRefer方法内的directory.subscribe方法中。

```java
directory.subscribe(subscribeUrl.addParameter(Constants.CATEGORY_KEY, 
                Constants.PROVIDERS_CATEGORY 
                + "," + Constants.CONFIGURATORS_CATEGORY 
                + "," + Constants.ROUTERS_CATEGORY));
```

订阅这3个路径providers、routers、configurators

#### 9.1 directory.subscribe

调用链为：
RegistryDirectory.subscribe ->FailbackRegistry.subscribe->- AbstractRegistry.subscribe>zookeeperRegistry.doSubscribe

```java
public void subscribe(URL url) {
    setConsumerUrl(url);
    registry.subscribe(url, this);
}

```

#### 9.2 FailbackRegistry. subscribe

​		调用FailbackRegistry.subscribe 进行订阅，这里有一个特殊处理，如果订阅失败，则会添加到定时任务中进行重试

```java
@Override
public void subscribe(URL url, NotifyListener listener) {
    super.subscribe(url, listener);
    removeFailedSubscribed(url, listener);
    try {
        // 向服务器端发送订阅请求
        doSubscribe(url, listener);

```

#### 9.3 zookeeperRegistry. doSubscribe

调用zookeeperRegistry执行真正的订阅操作，这段代码太长，我就不贴出来了，这里面主要做两个操作

- 对providers/routers/configurator三个节点进行创建和监听

- 调用notify(url,listener,urls) 将已经可用的列表进行通知



#### 9.4 AbstractRegistry.notify

```java
protected void notify(List<URL> urls) {
        if(urls == null || urls.isEmpty()) return;
        
        for (Map.Entry<URL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
            URL url = entry.getKey();
            
            if(! UrlUtils.isMatch(url, urls.get(0))) {
                continue;
            }
            
            Set<NotifyListener> listeners = entry.getValue();
            if (listeners != null) {
                for (NotifyListener listener : listeners) {
                    try {
                        notify(url, listener, filterEmpty(url, urls));
                    } catch (Throwable t) {
                        logger.error("Failed to notify registry event, urls: " +  urls + ", cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }
```

### 十 、调用流程



#### 10.1  消费端时序图

![1601566448626](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601566448626.png)



#### 10.2 流程图

![1601566753607](C:\Users\zhao\AppData\Roaming\Typora\typora-user-images\1601566753607.png)



#### 10.3  服务消费端调用链

##### 10.3.1  调用链的整体流程图

![img](https://user-gold-cdn.xitu.io/2018/10/4/1663e768c1fd43bd?w=800&h=738&f=png&s=460235)

​		蓝色部分是消费端的调用过程，大致过程分为Proxy-->Filter-->Invoker-->Directory-->LoadBalance-->Filter-->Invoker-->Client接着我们再来看一张集群容错的架构图，在集群调用失败时，Dubbo 提供了多种容错方案，缺省为 failover 重试。

![img](https://user-gold-cdn.xitu.io/2018/10/4/1663e72eeec155c4?w=600&h=300&f=png&s=119774)

对比一下两张图可以发现消费端的消费过程其实主要就是Dubbo的集群容错过程，下面开始分析源码

##### 10.3.2 源码入口

```java
public class Consumer {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"META-INF/spring/dubbo-demo-consumer.xml"});
        context.start(); 
        //这是服务引用的源码入口，获取代理类
        DemoService demoService = (DemoService) context.getBean("demoService"); // 获取远程服务代理
        //这是服务调用链的源码入口
        String hello = demoService.sayHello("world"); // 执行远程方法
        System.out.println(hello); // 显示调用结果
    }
}
```

ps:  demoService是一个proxy代理类，执行demoService.sayHello，其实是调用InvokerInvocationHandler.invoke方法，应该还记得proxy代理类中我们new了一个InvokerInvocationHandler实例

```java
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        ···
        return invoker.invoke(new RpcInvocation(method, args)).recreate();
    }
```

invoker=MockClusterWrapper(FaileOverCluster)，new RpcInvocation是将所有请求参数都会转换为RpcInvocation，接下来我们进入集群部分

##### 10.3.3 进入集群

进入MockClusterWrapper.invoke方法

```java
public Result invoke(Invocation invocation) throws RpcException {
        Result result = null;
        String value = directory.getUrl().getMethodParameter(invocation.getMethodName(), Constants.MOCK_KEY, Boolean.FALSE.toString()).trim();
        if (value.length() == 0 || value.equalsIgnoreCase("false")) {
            //no mock
            result = this.invoker.invoke(invocation);
        } else if (value.startsWith("force")) {
            if (logger.isWarnEnabled()) {
                logger.info("force-mock: " + invocation.getMethodName() + " force-mock enabled , url : " + directory.getUrl());
            }
            //force:direct mock
            result = doMockInvoke(invocation, null);
        } else {
            //fail-mock(没有配置mock)
            try {
                result = this.invoker.invoke(invocation);
            } catch (RpcException e) {
                if (e.isBiz()) {
                    throw e;
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.info("fail-mock: " + invocation.getMethodName() + " fail-mock enabled , url : " + directory.getUrl(), e);
                    }
                    result = doMockInvoke(invocation, e);
                }
            }
        }
        return result;
    }
```

没有配置mock，直接进入FaileOverCluster.invoke方法，其实是进入父类AbstractClusterInvoker.invoke方法

```java
public Result invoke(final Invocation invocation) throws RpcException {

        checkWhetherDestroyed();

        LoadBalance loadbalance;

        List<Invoker<T>> invokers = list(invocation);
    		//只有一个提供者
        if (invokers != null && invokers.size() > 0) {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl()
                    .getMethodParameter(invocation.getMethodName(), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        } else {
            //有多个提供者
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(Constants.DEFAULT_LOADBALANCE);
        }
        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
        return doInvoke(invocation, invokers, loadbalance);
    }
```

list(invocation)方法

```java
protected List<Invoker<T>> list(Invocation invocation) throws RpcException {
        List<Invoker<T>> invokers = directory.list(invocation);
        return invokers;
    }
```

##### 10.3.4 进入目录查找

directory.list(invocation)方法，这里directory=RegistryDirectory,进入RegistryDirectory.list方法

```java
public List<Invoker<T>> list(Invocation invocation) throws RpcException {
       ···
        List<Invoker<T>> invokers = doList(invocation);
        List<Router> localRouters = this.routers; // local reference
        if (localRouters != null && localRouters.size() > 0) {
            for (Router router : localRouters) {
                try {
                    if (router.getUrl() == null || router.getUrl().getParameter(Constants.RUNTIME_KEY, true)) {
                        invokers = router.route(invokers, getConsumerUrl(), invocation);
                    }
                ···
        return invokers;
    }
```

再进入doList方法：

```java
public List<Invoker<T>> doList(Invocation invocation) {
        if (forbidden) {
            // 1. 没有服务提供者 2. 服务提供者被禁用
            throw new RpcException(RpcException.FORBIDDEN_EXCEPTION,
                "No provider available from registry " + getUrl().getAddress() + " for service " + ··
        }
        List<Invoker<T>> invokers = null;
        Map<String, List<Invoker<T>>> localMethodInvokerMap = this.methodInvokerMap; // local reference
        ···
        return invokers == null ? new ArrayList<Invoker<T>>(0) : invokers;
    }
```

从this.methodInvokerMap里面查找一个 List<Invoker>返回

##### 10.3.5 进入路由

进入路由，返回到AbstractDirectory.list方法，进入router.route()方法，此时的router=MockInvokersSelector

```java
public <T> List<Invoker<T>> route(final List<Invoker<T>> invokers,
                                      URL url, final Invocation invocation) throws RpcException {
        if (invocation.getAttachments() == null) {
            return getNormalInvokers(invokers);
        } else {
            String value = invocation.getAttachments().get(Constants.INVOCATION_NEED_MOCK);
            if (value == null)
                return getNormalInvokers(invokers);
            else if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                return getMockedInvokers(invokers);
            }
        }
        return invokers;
    }
```

进入getNockedInvokers()方法，这个方法就是将传入的invokers和设置的路由规则匹配，获得符合条件的invokers返回

```java
private <T> List<Invoker<T>> getNormalInvokers(final List<Invoker<T>> invokers) {
        if (!hasMockProviders(invokers)) {
            return invokers;
        } else {
            List<Invoker<T>> sInvokers = new ArrayList<Invoker<T>>(invokers.size());
            for (Invoker<T> invoker : invokers) {
                if (!invoker.getUrl().getProtocol().equals(Constants.MOCK_PROTOCOL)) {
                    sInvokers.add(invoker);
                }
            }
            return sInvokers;
        }
    }
```

##### 10.3.6 进入负载均衡

继续回到AbstractClusterInvoker.invoke方法，

```java
public Result invoke(final Invocation invocation) throws RpcException {
        ···
        if (invokers != null && invokers.size() > 0) {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl()
                    .getMethodParameter(invocation.getMethodName(), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        } 
        ···
        return doInvoke(invocation, invokers, loadbalance);
    }
```

loadbalance扩展点适配器LoadBalance$Adaptive，默认是RandomLoadBalance随机负载，所以loadbalance=RandomLoadBalance，进入FailoverClusterInvoker.doInvoke方法

```java
public Result doInvoke(Invocation invocation, final List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        ···
            Invoker<T> invoker = select(loadbalance, invocation, copyinvokers, invoked);
            invoked.add(invoker);
            RpcContext.getContext().setInvokers((List) invoked);
            try {
                Result result = invoker.invoke(invocation);
                if (le != null && logger.isWarnEnabled()) {
                    logger.warn("Although retry the method " + invocation.getMethodName()
                           ····);
                }
                return result;
            } catch (RpcException e) {
               ···
            } finally {
                providers.add(invoker.getUrl().getAddress());
            }
        }
       ···
    }
```

进入select(loadbalance, invocation, copyinvokers, invoked)方法，最终进入RandomLoadBalance.doSelect()方法，这个随机算法中可以配置权重，Dubbo根据权重最终选择一个invoker返回

##### 10.3.7 远程调用

​		回到 FaileOverCluster.doInvoke方法中，执行Result result = invoker.invoke(invocation);此时的invoker就是负载均衡选出来的invoker=RegistryDirectory$InvokerDelegete,走完8个Filter，进DubboInvoker.doInvoke()方法

```java
protected Result doInvoke(final Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation) invocation;
        final String methodName = RpcUtils.getMethodName(invocation);
        inv.setAttachment(Constants.PATH_KEY, getUrl().getPath());
        inv.setAttachment(Constants.VERSION_KEY, version);

        ExchangeClient currentClient;
        if (clients.length == 1) {
            currentClient = clients[0];
        } else {
            currentClient = clients[index.getAndIncrement() % clients.length];
        }
        try {
            boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
            boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
            int timeout = getUrl().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
            if (isOneway) {
                boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
                currentClient.send(inv, isSent);
                RpcContext.getContext().setFuture(null);
                return new RpcResult();
            } else if (isAsync) {
                ResponseFuture future = currentClient.request(inv, timeout);
                RpcContext.getContext().setFuture(new FutureAdapter<Object>(future));
                return new RpcResult();
            } else {
                RpcContext.getContext().setFuture(null);
                return (Result) currentClient.request(inv, timeout).get();
            }
        } catch (TimeoutException e) {
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        } catch (RemotingException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
```

这里为什么DubboInvoker是个protocol? 因为RegistryDirectory.refreshInvoker.toInvokers： protocol.refer，我们进入currentClient.request(inv, timeout).get()方法，进入HeaderExchangeChannel.request方法，进入NettyChannel.send方法，

```java
public void send(Object message, boolean sent) throws RemotingException {
        super.send(message, sent);

        boolean success = true;
        int timeout = 0;
        try {
            ChannelFuture future = channel.write(message);
            if (sent) {
                timeout = getUrl().getPositiveParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
                success = future.await(timeout);
            }
           ···
    }
```

这里最终执行ChannelFuture future = channel.write(message)，通过Netty发送网络请求

#### 10.4 服务端调用处理链

##### 10.4.1 NettyHandler. messageReceived

```java
@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
        try {
            handler.received(channel, e.getMessage());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }
```

接收消息的时候，通过NettyHandler.messageReceived作为入口

##### 10.4.2 handler.received

这个handler是什么呢？还记得在服务发布的时候，组装了一系列的handler吗？代码如下：

```java
public class HeaderExchanger implements Exchanger {
    
    public static final String NAME = "header";

    public ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeClient(Transporters.connect(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
    }

    public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
    }
}
```

##### 10.4.3 NettyServer

接着又在Nettyserver中，wrap了多个handler

```java
 public NettyServer(URL url, ChannelHandler handler) throws RemotingException{
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
    }
```

所以服务端的handler处理链为：

MultiMessageHandler(HeartbeatHandler(AllChannelHandler(DecodeHandler)))

MultiMessageHandler: 复合消息处理

HeartbeatHandler：心跳消息处理，接收心跳并发送心跳响应

AllChannelHandler：业务线程转化处理器，把接收到的消息封装成ChannelEventRunnable可执行任务给线程池处理

DecodeHandler:业务解码处理器

##### 10.4.4 Header.received

```java
public void received(Channel channel, Object message) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            if (message instanceof Request) {
                // handle request.
                Request request = (Request) message;
                if (request.isEvent()) {
                    handlerEvent(channel, request);
                } else {
                    if (request.isTwoWay()) {
                        Response response = handleRequest(exchangeChannel, request);
                        channel.send(response);
                    } else {
                        handler.received(exchangeChannel, request.getData());
                    }
                }
            } else if (message instanceof Response) {
                handleResponse(channel, (Response) message);
            } else if (message instanceof String) {
                if (isClientSide(channel)) {
                    Exception e = new Exception("Dubbo client can not supported string message: " + message + " in channel: " + channel + ", url: " + channel.getUrl());
                    logger.error(e.getMessage(), e);
                } else {
                    String echo = handler.telnet(channel, (String) message);
                    if (echo != null && echo.length() > 0) {
                        channel.send(echo);
                    }
                }
            } else {
                handler.received(exchangeChannel, message);
            }
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }
```

交互层请求响应处理，有三种处理方式

- handlerRequest，双向请求

- handler.received 单向请求

- andleResponse 响应消息

###### 10.4.4.1 handleRequest

处理请求并返回response

```java
Response handleRequest(ExchangeChannel channel, Request req) throws RemotingException {
        Response res = new Response(req.getId(), req.getVersion());
        if (req.isBroken()) {
            Object data = req.getData();

            String msg;
            if (data == null) msg = null;
            else if (data instanceof Throwable) msg = StringUtils.toString((Throwable) data);
            else msg = data.toString();
            res.setErrorMessage("Fail to decode request due to: " + msg);
            res.setStatus(Response.BAD_REQUEST);

            return res;
        }
        // find handler by message class.
        Object msg = req.getData();
        try {
            // handle data.
            Object result = handler.reply(channel, msg);
            res.setStatus(Response.OK);
            res.setResult(result);
        } catch (Throwable e) {
            res.setStatus(Response.SERVICE_ERROR);
            res.setErrorMessage(StringUtils.toString(e));
        }
        return res;
    }
```

###### 10.4.4.2 ExchangeHandlerAdaptive.replay(DubboProtocol)

调用DubboProtocol中定义的ExchangeHandlerAdaptive.replay方法处理消息

```JAVA
private ExchangeHandler requestHandler = new ExchangeHandlerAdapter() {
    
    public Object reply(ExchangeChannel channel, Object message) throws RemotingException {
     invoker.invoke(inv);
}

```

接下来invoker.invoke会调用哪个类中的方法呢？还记得在RegistryDirectory中发布本地方法的时候，对invoker做的包装吗？通过InvokerDelegete对原本的invoker做了一层包装，而原本的invoker是什么呢？是一个JavassistProxyFactory生成的动态代理吧。所以此处的invoker应该是

Filter(Listener(InvokerDelegete(AbstractProxyInvoker (Wrapper.invokeMethod)))

RegistryDirectory生成invoker的代码如下:

```JAVA
private <T> ExporterChangeableWrapper<T>  doLocalExport(final Invoker<T> originInvoker){
    String key = getCacheKey(originInvoker);
    ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
    if (exporter == null) {
        synchronized (bounds) {
            exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
            if (exporter == null) {
                final Invoker<?> invokerDelegete = new InvokerDelegete<T>(originInvoker, getProviderUrl(originInvoker));
                exporter = new ExporterChangeableWrapper<T>((Exporter<T>)protocol.export(invokerDelegete), originInvoker);
                bounds.put(key, exporter);
            }
        }
    }
    return (ExporterChangeableWrapper<T>) exporter;
}

```

### 十一、 Directory

​			集群目录服务Directory， 代表多个Invoker, 可以看成List<Invoker>,它的值可能是动态变化的比如注册中心推送变更。集群选择调用服务时通过目录服务找到所有服务

StaticDirectory: 静态目录服务， 它的所有Invoker通过构造函数传入， 服务消费方引用服务的时候， 服务对多注册中心的引用，将Invokers集合直接传入 StaticDirectory构造器，再由Cluster伪装成一个Invoker；StaticDirectory的list方法直接返回所有invoker集合；

RegistryDirectory: 注册目录服务， 它的Invoker集合是从注册中心获取的， 它实现了NotifyListener接口实现了回调接口notify(List<Url>)

#### 11.1 Directory目录服务的更新过程

​			RegistryProtocol.doRefer方法，也就是消费端在初始化的时候，这里涉及到了RegistryDirectory这个类。然后执行cluster.join(directory)方法。cluster.join其实就是将Directory中的多个Invoker伪装成一个Invoker, 对上层透明，包含集群的容错机制

```java
 private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);
        URL subscribeUrl = new URL(Constants.CONSUMER_PROTOCOL, NetUtils.getLocalHost(), 0, type.getName(), directory.getUrl().getParameters());
        if (! Constants.ANY_VALUE.equals(url.getServiceInterface())
                && url.getParameter(Constants.REGISTER_KEY, true)) {
            registry.register(subscribeUrl.addParameters(Constants.CATEGORY_KEY, Constants.CONSUMERS_CATEGORY,
                    Constants.CHECK_KEY, String.valueOf(false)));
        }
        directory.subscribe(subscribeUrl.addParameter(Constants.CATEGORY_KEY,
                Constants.PROVIDERS_CATEGORY
                + "," + Constants.CONFIGURATORS_CATEGORY 
                + "," + Constants.ROUTERS_CATEGORY));
        return cluster.join(directory);
    }
```

#### 11.2 directory.subscribe

订阅节点的变化，

- 当zookeeper上指定节点发生变化以后，会通知到RegistryDirectory的notify方法

- 将url转化为invoker对象