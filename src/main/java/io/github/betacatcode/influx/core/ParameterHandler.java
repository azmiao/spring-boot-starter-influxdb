package io.github.betacatcode.influx.core;

import io.github.betacatcode.influx.ano.Param;

import java.lang.reflect.Parameter;

/**
 * 参数处理器
 */
public class ParameterHandler {

    /**
     * 拼接sql
     *
     * @param parameters 参数名
     * @param args       参数实际值
     * @param sql        未拼接参数的sql语句
     * @return 拼接好的sql
     */
    public String handleParameter(Parameter[] parameters, Object[] args, String sql) {
        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameterType = parameters[i].getType();
            String parameterName = parameters[i].getName();

            Param param = parameters[i].getAnnotation(Param.class);
            if (param != null) {
                parameterName = param.value();
            }

            sql = sql.replaceAll("\\#\\{" + parameterName + "\\}", "\"" + args[i] + "\"");
            sql = sql.replaceAll("\\^\\{" + parameterName + "\\}", "'" + args[i] + "'");
            sql = sql.replaceAll("\\$\\{" + parameterName + "\\}", args[i].toString());
        }

        System.out.println("influxDB SQL=" + sql);
        return sql;
    }
}
