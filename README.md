# auxiliary-tools
![Iguos Tools](https://www.iguos.com/custom/icon.png)

### 1、继承引用
```
<dependencies>

    # auxiliary-interface-log
    <dependency>
        <groupId>com.iguos.common</groupId>
        <artifactId>auxiliary-interface-log</artifactId>
    </dependency>
    
    # auxiliary-servlet-tools
    <dependency>
        <groupId>com.iguos.common</groupId>
        <artifactId>auxiliary-servlet-tools</artifactId>
    </dependency>
    
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.iguos.common</groupId>
            <artifactId>auxiliary-tools</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
### 2、单独引用
```
# auxiliary-interface-log
<dependency>
    <groupId>com.iguos.common</groupId>
    <artifactId>auxiliary-interface-log</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

# auxiliary-servlet-tools
<dependency>
    <groupId>com.iguos.common</groupId>
    <artifactId>auxiliary-servlet-tools</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## 使用说明
#### 1、auxiliary-interface-log 
接口打印日志
> auxiliary.interface-log.enable: true/false

生成日志
> 在类或方法上添加注解 @AnalysisDocument


