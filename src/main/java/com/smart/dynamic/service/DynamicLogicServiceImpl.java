package com.smart.dynamic.service;

import com.alibaba.fastjson2.JSON;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.smart.dynamic.defines.DynamicGrammar;
import com.smart.dynamic.defines.DynamicLogicDefine;
import com.smart.dynamic.defines.DynamicResult;
import com.smart.dynamic.defines.MatchItem;
import com.smart.dynamic.defines.ParamDefine;
import com.smart.dynamic.defines.act.SQLAct;
import com.smart.dynamic.enums.DynamicSQLType;
import com.smart.dynamic.enums.ParamType;
import com.smart.dynamic.exceptions.DynamicException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author H.Y.F
 * @Date 2024/4/12 13:18
 * @Version V1.0
 */
@Service
public class DynamicLogicServiceImpl implements IDynamicLogicService {
    public static final Logger LOG = LoggerFactory.getLogger(DynamicLogicServiceImpl.class);

    @Override
    public String generateMarkDown() {
        List<Record> rs = Db.find("SELECT * FROM " + TABLE);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < rs.size(); i++) {
            Record r = rs.get(i);
            DynamicLogicDefine logic = recordToVo(r);
            // boolean hasSave = logic.getGrammar().getActs().stream().anyMatch(a ->
            // a.getType() == SIMPLE_SAVE);
            // if(hasSave) {
            // continue;
            // }
            // 添加标题(###)
            text.append("\n\n");
            text.append(String.format("### 2.%d %s (%d)", i + 1, logic.getTitle(), logic.getId()));
            // 备注
            text.append("\n");
            text.append(logic.getNote());
            // 参数表
            List<ParamDefine> params = logic.getGrammar().getParams();
            boolean hasPage = logic.getGrammar().getActs().stream()
                    .anyMatch(a -> a.getType() == DynamicSQLType.FIND_PAGE);
            if (hasPage) {
                params = params == null ? new ArrayList<>() : params;
                ParamDefine pageNum = new ParamDefine();
                pageNum.setMust(false);
                pageNum.setAlias("pageNum");
                pageNum.setType(ParamType.INT);
                pageNum.setNote("查询页码,默认1");
                ParamDefine pageSize = new ParamDefine();
                pageSize.setMust(false);
                pageSize.setAlias("pageSize");
                pageSize.setType(ParamType.INT);
                pageSize.setNote("每页条数,默认20");

                params.add(pageNum);
                params.add(pageSize);
            }
            if (CollectionUtils.isEmpty(params)) {
                continue;
            }

            text.append("\n");
            text.append("| data参数名 | 类型 | 是否必填 | 备注 |");
            text.append("\n");
            text.append("|:----|:----|:----|:----|");
            for (ParamDefine param : params) {
                text.append("\n");
                text.append(String.format("| %s | %s | %s | %s |", param.getAlias(), param.getType().name(),
                        param.isMust(), param.getNote()));
            }
        }
        // String dashboard = String.format("# 概览\n" + "\n" +
        // "目前已定义%d个动态查询，假设动态查询定义一般在15分钟内完成，每个接口开发时间为30分钟，大约节约开发资源 %.2f 工时。\n"
        // ,rs.size(),15.00 * rs.size() / 60);
        return MARKDOWN_START + text;
    }

    @Override
    public Page<DynamicLogicDefine> list(int pageNum, int pageSize, String keyword) {
        SqlPara para = new SqlPara();
        if (StringUtils.isNotBlank(keyword)) {
            para.setSql("SELECT * FROM " + TABLE + " WHERE title like '%" + keyword + "%'");
        } else {
            para.setSql("SELECT * FROM " + TABLE);
        }
        Page<Record> page = Db.paginate(pageNum, pageSize, para);
        List<DynamicLogicDefine> res = Optional.of(page.getList())
                .filter(CollectionUtils::isNotEmpty)
                .map(ls -> ls.stream().map(this::recordToVo).collect(Collectors.toList()))
                .orElse(new ArrayList<>());
        return new Page<>(res, pageNum, pageSize, page.getTotalPage(), page.getTotalRow());
    }

    @Override
    public DynamicLogicDefine detail(Long id) {
        Record r = Db.findFirst("SELECT * FROM " + TABLE + " WHERE id=?", id);
        return recordToVo(r);
    }

    private DynamicLogicDefine recordToVo(Record r) {
        if (r == null) {
            return null;
        }
        DynamicLogicDefine query = new DynamicLogicDefine();
        query.setId(r.getLong("id"));
        query.setTitle(r.getStr("title"));
        query.setNote(r.getStr("note"));
        query.setGrammar(JSON.parseObject(r.getStr("grammar"), DynamicGrammar.class));
        return query;
    }

    @Override
    public DynamicLogicDefine save(DynamicLogicDefine query) {
        // valid
        validDefine(query);
        // save
        Record r = new Record();
        r.set("title", query.getTitle());
        r.set("note", query.getNote());
        r.set("grammar", JSON.toJSONString(query.getGrammar()));
        if (query.getId() == null) {
            r.set("id", UUID.randomUUID());
            Db.save(TABLE, r);
        } else {
            r.set("id", query.getId());
            Db.update(TABLE, r);
        }
        query.setId(r.getLong("id"));
        return query;
    }

    private void validDefine(DynamicLogicDefine dynamicDefine) {
        DynamicGrammar grammar = dynamicDefine.getGrammar();
        if (grammar == null) {
            throw new DynamicException("基础语法不能为空");
        }
        if (CollectionUtils.isEmpty(grammar.getActs())) {
            throw new DynamicException("至少有一个行为");
        }
        List<SQLAct> acts = grammar.getActs();
        for (int i = 0; i < acts.size(); i++) {
            SQLAct act = acts.get(i);
            if (act.getType() == null) {
                throw new DynamicException("类型不能为空");
            }
            if (i > 0 && StringUtils.isBlank(act.getAlias())) {
                throw new DynamicException("多个行为需要指定别名");
            }
        }

        Set<String> aliasSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(grammar.getParams())) {
            for (ParamDefine param : grammar.getParams()) {
                if (StringUtils.isBlank(param.getAlias())) {
                    throw new DynamicException("参数名不能为空");
                }
                if (param.getType() == null) {
                    throw new DynamicException("参数类型不能为空");
                }
                aliasSet.add(param.getAlias());
            }
        }
        // 验证${} 和 #{}包裹的变量是否定义
        Set<String> variables = findVariables(JSON.toJSONString(grammar.getActs()));
        for (String variable : variables) {
            variable = variable.trim();
            if (variable.startsWith("user.")) {
                continue;
            }
            if (variable.startsWith("act.")) {
                continue;
            }
            if (!aliasSet.contains(variable)) {
                throw new RuntimeException(variable + "参数未定义");
            }
        }
    }

    private static Set<String> findVariables(String queryString) {
        String pattern = "[$|#]\\{([^{}]+)\\}";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(queryString);
        Set<String> variables = new HashSet<>();
        while (matcher.find()) {
            String variable = matcher.group(1);
            variables.add(variable);
        }
        return variables;
    }

    @Override
    public DynamicResult<?> execute(DynamicLogicDefine define, Map<String, Object> data) {
        LOG.debug("执行动态SQL配置:{}", JSON.toJSONString(define));
        DynamicGrammar grammar = define.getGrammar();
        // 检查必填参数及参数约束,设置默认值
        validParamsAndSetDefault(grammar.getParams(), data);
        // 执行行为
        Map<String, Object> res = new HashMap<>();
        List<SQLAct> acts = grammar.getActs();
        boolean hasSave = acts.stream().anyMatch(a -> a.getType() == DynamicSQLType.SIMPLE_SAVE);
        if (hasSave) {
            // save行为
            Db.tx(() -> {
                runActs(data, acts, res);
                return true;
            });
        } else {
            // 查询行为
            runActs(data, acts, res);
        }

        // 组装返回值
        if (res.size() == 1) {
            Object r = new ArrayList<>(res.values()).get(0);
            if (r instanceof List) {
                return DynamicResult.success((List<?>) r);
            }
            if (r instanceof Page) {
                Page<?> page = (Page<?>) r;
                return DynamicResult.success(page);
            }
            return DynamicResult.success(r);
        }
        return DynamicResult.success(res);
    }

    @SuppressWarnings("unchecked")
    private void runActs(Map<String, Object> data, List<SQLAct> acts, Map<String, Object> res) {
        for (SQLAct act : acts) {
            Object r = runAct(act, data, res);
            if (act.getNeedReturn()) {
                res.put(act.getAlias(), r);
            }

            if (r == null) {
                if (StringUtils.isNotBlank(act.getErrorMsgWhenNull())) {
                    throw new RuntimeException(act.getErrorMsgWhenNull());
                }
                continue;
            }

            if (act.getType() == DynamicSQLType.FIND_ONE) {
                ((Map<String, Object>) r).forEach((k, v) -> data.put("act." + act.getAlias() + "." + k, v));
            } else {
                List<Map<String, Object>> records = new ArrayList<>();
                if (act.getType() == DynamicSQLType.FIND_PAGE) {
                    records = ((Page<Map<String, Object>>) r).getList();
                } else if (act.getType() == DynamicSQLType.FIND_LIST) {
                    records = (List<Map<String, Object>>) r;
                }
                for (int i = 0; i < records.size(); i++) {
                    for (Map.Entry<String, Object> entry : records.get(i).entrySet()) {
                        String k = entry.getKey();
                        Object v = entry.getValue();
                        data.put("act." + act.getAlias() + "[" + i + "]." + k, v);
                    }
                }
            }
        }
    }

    private Object runAct(SQLAct act, Map<String, Object> data, Map<String, Object> res) {
        // 组装sql
        String sql = buildPrepareSql(act, data);
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        LOG.info("执行SQL:{}", sql);
        // 执行语句
        SqlPara para = new SqlPara();
        para.setSql(sql);

        Object r;
        if (act.getType() == DynamicSQLType.SIMPLE_SAVE) {
            r = Db.update(para);
        } else if (act.getType() == DynamicSQLType.FIND_ONE) {
            r = Optional.ofNullable(Db.findFirst(para))
                    .map(Record::getColumns)
                    .orElse(null);
        } else if (act.getType() == DynamicSQLType.FIND_LIST) {
            r = Optional.ofNullable(Db.find(para))
                    .map(rs -> rs.stream().map(Record::getColumns).collect(Collectors.toList()))
                    .orElse(new ArrayList<>());
        } else if (act.getType() == DynamicSQLType.FIND_PAGE) {
            int pageSize = Optional.ofNullable(data.get("pageSize"))
                    .map(String::valueOf)
                    .map(Integer::valueOf)
                    .filter(n -> n > 0)
                    .orElse(20);
            int pageNum = Optional.ofNullable(data.get("pageNum"))
                    .map(String::valueOf)
                    .map(Integer::valueOf)
                    .filter(n -> n > 0)
                    .orElse(1);
            data.put("pageSize", pageSize);
            data.put("pageNum", pageNum);
            Page<Record> page = Db.paginate(pageNum, pageSize, para);
            List<Map<String, Object>> pageRes = Optional.ofNullable(page.getList())
                    .map(rs -> rs.stream().map(Record::getColumns).collect(Collectors.toList()))
                    .orElse(new ArrayList<>());
            r = new Page<>(pageRes, pageNum, pageSize, page.getTotalPage(), page.getTotalRow());
        } else {
            throw new RuntimeException("不支持的执行类型");
        }

        return r;
    }

    /**
     * @Description 验证参数树 <br/>
     * @Author H.Y.F
     * @ClassName com.qzsoft.lims.lcgl.mobile.dynamic.DynamicQueryServiceImpl.java
     * @Date 2024-04-12 15:34
     * @Version V1.0
     */
    private void validParamsAndSetDefault(List<ParamDefine> params, Map<String, Object> data) {
        if (CollectionUtils.isEmpty(params)) {
            return;
        }
        for (ParamDefine param : params) {
            String alias = param.getAlias();
            Object value = data.get(alias);
            ParamType type = param.getType();
            boolean valueIsNull = type.isNull(value);
            // 默认值
            if (param.getDefaultValue() != null && valueIsNull) {
                data.put(alias, param.getDefaultValue());
                continue;
            }
            // 验证必填项
            if (param.isMust() && valueIsNull) {
                String tip = Optional.ofNullable(param.getNote()).filter(StringUtils::isNotBlank).orElse(alias);
                throw new RuntimeException(tip + "不能为空");
            }
            if (valueIsNull) {
                continue;
            }

            // 转换为对应的类型防止SQL注入
            try {
                value = type.format(value);
                data.put(alias, value);
            } catch (Exception e) {
                LOG.error("格式转换出错,请检查格式", e);
                throw new RuntimeException("格式转换出错,请检查格式");
            }

            // 验证参数格式 并转换为对应类型
            if (StringUtils.isNotBlank(param.getValidRegex())) {
                Pattern pattern = Pattern.compile(param.getValidRegex());
                Matcher matcher = pattern.matcher(String.valueOf(value));
                if (!matcher.find()) {
                    throw new RuntimeException(String.format("%s不满足条件%s", alias, param.getValidRegex()));
                }
            }

            // 从数据库验证数据正确性
            String sql = param.getValidSql();
            if (StringUtils.isNotBlank(sql)) {
                int count = 0;
                for (int i = 0; i < sql.length(); i++) {
                    if (sql.charAt(i) == '?') {
                        count++;
                    }
                }
                List<Record> ls = Db.find(sql, Collections.nCopies(count, value).toArray());
                if (ls.isEmpty()) {
                    throw new RuntimeException(alias + "参数值未通过校验");
                }
            }
        }
    }

    /**
     * @Description 组装占位sql <br/>
     * @Author H.Y.F
     * @ClassName com.qzsoft.lims.lcgl.mobile.dynamic.DynamicQueryServiceImpl.java
     * @Date 2024-04-12 15:59
     * @Version V1.0
     */
    private String buildPrepareSql(SQLAct act, Map<String, Object> data) {
        String sql = act.getBase();
        // 1.解析条件语句
        String conditionSql = matchCondition(act.getConditions(), data);
        if (StringUtils.isNotBlank(conditionSql)) {
            if (StringUtils.isNotBlank(sql)) {
                if (!hasWhereCondition(sql)) {
                    sql += " WHERE 1=1 ";
                }
                sql = sql + conditionSql;
            } else {
                sql = conditionSql;
            }
        }

        if (StringUtils.isBlank(sql)) {
            return null;
        }

        // 2.替换参数值
        Set<String> variables = findVariables(sql);
        for (String variable : variables) {
            Object value = data.get(variable);
            sql = sql.replaceAll("\\$\\{" + variable.trim() + "\\}", paramTypeHandel(value, true));
            sql = sql.replaceAll("#\\{" + variable.trim() + "\\}", paramTypeHandel(value, false));
        }

        if (StringUtils.isNotBlank(act.getOrderBy())) {
            sql += " " + act.getOrderBy();
        }
        return sql;
    }

    private String matchCondition(List<MatchItem> conditions, Map<String, Object> data) {
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        for (MatchItem item : conditions) {
            String itemSql = null;
            try {
                if (item.match(data)) {
                    itemSql = item.getSql();
                }
            } catch (Exception e) {
                LOG.error("", e);
                throw new DynamicException("条件匹配错误,请联系管理员");
            }
            if (itemSql != null && StringUtils.isNotBlank(itemSql)) {
                sql.append(" ");
                sql.append(itemSql.trim());
                sql.append(" ");
            }
        }
        return sql.toString();
    }

    private static boolean hasWhereCondition(String sql) {
        // 定义正则表达式：不会匹配子查询的where
        String regex = "\\bWHERE\\b(?!\\s*(AND|OR)\\b)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        return matcher.find(); // 返回匹配结果
    }

    private String paramTypeHandel(Object value, boolean formatter) {
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            return values.stream().map(o -> paramTypeHandel(o, formatter)).collect(Collectors.joining(","));
        }
        if (!formatter) {
            return String.valueOf(value);
        }
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return String.valueOf(value);
    }
}
