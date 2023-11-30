//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.github.betacatcode.influx.core;

import org.influxdb.InfluxDB;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Executor {

    // 数据库名称
    @Value("${spring.influx.database}")
    private String dbName;

    InfluxDB influxDB;
    InfluxDBMapper influxDBMapper;

    public Executor() {
    }

    public Executor(InfluxDB influxDB, InfluxDBMapper influxDBMapper) {
        this.influxDB = influxDB;
        this.influxDBMapper = influxDBMapper;
    }

    /**
     * 设置配置
     *
     * @param measurement 注解
     */
    private void setInfluxConfig(Measurement measurement) {
        // 数据库
        String database = measurement.database();
        if ("[unassigned]".equals(database) && !StringUtils.isEmpty(dbName)) {
            database = dbName;
        } else if ("[unassigned]".equals(database)) {
            throw new IllegalArgumentException(Measurement.class.getSimpleName() + " should specify a database value for this operation");
        }
        this.influxDB.setDatabase(database);
        // 保留策略
        String retentionPolicy = measurement.retentionPolicy();
        this.influxDB.setRetentionPolicy(retentionPolicy);
    }

    public <E> List<E> select(String sql, Class<E> domainClass) {
        Measurement measurement = (Measurement)domainClass.getAnnotation(Measurement.class);
        setInfluxConfig(measurement);
        QueryResult queryResult = this.influxDB.query(new Query(sql));
        return this.influxDBMapper.toPOJO(queryResult, domainClass);
    }

    public void insert(Object args[]) {
        if (args.length != 1) {
            throw new RuntimeException();
        }
        Object obj = args[0];
        //插入的是集合类型的
        if (obj instanceof List) {
            List list = (ArrayList) obj;
            if (list.size() > 0) {
                Object firstObj = list.get(0);
                Class<?> domainClass = firstObj.getClass();
                List<Point> pointList = new ArrayList<>();
                for (Object o : list) {
                    Point point = Point
                            .measurementByPOJO(domainClass)
                            .addFieldsFromPOJO(o)
                            .build();
                    pointList.add(point);
                }

                //获取数据库名和rp
                Measurement measurement = firstObj.getClass().getAnnotation(Measurement.class);
                String retentionPolicy = measurement.retentionPolicy();
                BatchPoints batchPoints = BatchPoints
                        .builder()
                        .points(pointList)
                        .retentionPolicy(retentionPolicy).build();
                setInfluxConfig(measurement);
                influxDB.write(batchPoints);
            }

            //插入单个
        } else {
            influxDBMapper.save(obj);
        }
    }

    public void delete(String sql, String database) {
        influxDB.query(new Query(sql, database));
    }

}
