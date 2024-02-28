# spring-boot-starter-influxdb-fork

## 简介

这是原版[spring-boot-starter-influxdb](https://github.com/betacatcode/spring-boot-starter-influxdb)的修改版本

需要和influxDB官方依赖配合使用

修改内容：
- `@Select`注解会自动使用返回类型的数据库进行查询
- 添加默认的数据库查询配置支持
- `@Select`注解增加返回值泛型支持，详情看本页最后
- 可选解析返回实体的表名匹配方式

## 使用方法

1.  下载[releases](https://github.com/azmiao/spring-boot-starter-influxdb/releases)下的依赖，并自己导入项目，比如gradle的:

~~~
implementation fileTree(dir: 'libs', includes: ['*.jar'])
~~~

2. 配置yml文件

~~~yaml
spring:
  influx:
    url: http://127.0.0.1:8086
    password: 123456
    dbName: db_test
    user: admin
    mapper-location: com.github.betacatcode
    measurementMatch: equals
~~~

**其中 mapper-location 是InfluxDB Mapper存放路径** 

**其中 dbName 你需要的默认数据库名，在使用注解@Measurement()将默认使用配置的dbName** 

**其中 measurementMatch 解析返回实体的时候表名匹配方式，可选[equals, contains, false]，默认equals；完全匹配，contains：模糊匹配，false：不校验表名** 

3. 建立代理mapper的配置类，否则可能会出现注入优先级引起的问题
~~~
@DependsOn("proxyMapperRegister")
@Configuration
public class ProxyMapperConfig {
}
~~~

4. 创建表对应实体类（此处使用lombok依赖，也可不使用）

~~~java
import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.annotation.TimeColumn;

import java.time.Instant;

@Data
@Measurement(database = "test",name = "student")
public class Student {
    private String id;

    @Column(name = "sname",tag = true)
    private String sname;

    @Column(name = "value")
    private Double value;

    @TimeColumn
    @Column(name = "time")
    private Instant time;
}
~~~

5. 创建实体类对应Mapper，需继承InfluxDBBaseMapper这个接口

> 【注意】：`@Select`中的sql语句：
- 使用`#{}`会被替代为`"`
- 使用`^{}`会被替代为`'`
- 使用`${}`将不会带引号

~~~java
public interface StudentMapper extends InfluxDBBaseMapper {

    @Select(value = "select * from test.autogen.student where sname=#{sname}",resultType = Student.class)
    List<Student> selectByName(String sname);

    @Delete(value = "delete from student",database = "test")
    void deleteAll();

    @Insert
    void insertOne(Student student);

    @Insert
    void insertBatch(List<Student> students);

}
~~~

6. 建立测试类测试

~~~java
@RunWith(SpringRunner.class)
@SpringBootTest
class InfluxdbAnnotationApplicationTests {

	@Autowired
	StudentMapper studentMapper;

	@Test
	void contextLoads() {
	}

	//单条插入
	@Test
	void testInsertOne(){
		Student student1 = new Student();
		student1.setSname("ww");
		student1.setValue(235.12);
		student1.setTime(Instant.ofEpochMilli(1640966500000l));
		studentMapper.insertOne(student1);
	}

	//批量插入
	@Test
	void testInsertBatch(){
		Student student1 = new Student();
		student1.setSname("zs");
		student1.setValue(123.45);
		student1.setTime(Instant.ofEpochMilli(1640966400000l));

		Student student2 = new Student();
		student2.setSname("ls");
		student2.setValue(666.21);
		student2.setTime(Instant.ofEpochMilli(1640966300000l));

		List<Student> studentList = new ArrayList<>();

		studentList.add(student1);
		studentList.add(student2);
		studentMapper.insertBatch(studentList);
	}

	//查询
	@Test
	void testSelect(){
		List<Student> studentList = studentMapper.selectByName("zs");
		for (Student student : studentList) {
			System.out.println(student);
		}
	}

	//删除
	@Test
	void testDelete(){
		studentMapper.deleteAll();
	}

}

~~~


### `@Select`注解返回值泛型支持

```java
public interface StudentMapper<T> extends InfluxDBBaseMapper<T> {

    @Select(value = "select * from test.autogen.student where sname=#{sname}",resultType = ParamClass.class)
    List<T> selectByName(@Param("sname") String sname, @ResultClass Class<T> clazz);

}
```




