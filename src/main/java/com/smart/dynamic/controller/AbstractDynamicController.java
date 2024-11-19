package com.smart.dynamic.controller;

import com.smart.dynamic.defines.DynamicLogicDefine;
import com.smart.dynamic.defines.DynamicResult;
import com.smart.dynamic.enums.DynamicSQLType;
import com.smart.dynamic.exceptions.DynamicException;
import com.smart.dynamic.service.IDynamicLogicService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

/**
 * @Description 动态查询c层 <br/>
 * @Author H.Y.F
 * @Date 2024-04-16 10:46
 * @Version V1.0
 */
@RestController
@RequestMapping("/v1/dynamic")
public abstract class AbstractDynamicController {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDynamicController.class);
    @Resource
    private IDynamicLogicService dynamicLogicService;

    /**
     * 根据已存在的查询定义 生成 MarkDown,方便前后端联调
     *
     * @return markdown文本
     */
    @GetMapping("/doc")
    public void generateMarkDown(HttpServletResponse response) {
        String content = dynamicLogicService.generateMarkDown();
        // 将字符串转为字节数组
        byte[] data = content.getBytes(StandardCharsets.UTF_8);

        try {
            // 设置响应的内容类型为 application/octet-stream （二进制流）
            response.setContentType("application/octet-stream");
            // 设置输出流的长度
            response.setContentLength(data.length);
            // 设置文件名（可选）
            String fileName = URLEncoder.encode("联调文档.md", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            // 将数据写入 HttpServletResponse 的输出流
            response.getOutputStream().write(data);
        } catch (IOException e) {
            LOG.error("", e);
        }
    }

    @GetMapping("/list")
    public Object list(@RequestParam("keyword") String keyword,
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        return dynamicLogicService.list(pageNum, pageSize, keyword);
    }

    @GetMapping("/detail")
    public Object detail(@RequestParam Long id) {
        return dynamicLogicService.detail(id);
    }

    @PostMapping("/save")
    public Object save(@RequestBody DynamicLogicDefine query) {
        return dynamicLogicService.save(query);
    }

    @GetMapping("/call")
    public Object call(HttpServletRequest request) {
        Map<String,Object> paramsMap =  new HashMap<>();
        for (Entry<String,String[]> paramEntry : request.getParameterMap().entrySet()) {
            paramsMap.put(paramEntry.getKey(), paramEntry.getValue()[0]);
        }
        Long dynamicId = Optional.ofNullable(paramsMap.get("dynamicId"))
                .map(String::valueOf)
                .map(Long::parseLong)
                .orElseThrow(() -> new DynamicException("dynamicId 不能为空"));
        DynamicLogicDefine define = dynamicLogicService.detail(dynamicId);
        if (define == null) {
            return DynamicResult.fail("未找到动态SQL规则");
        }
        boolean hasSave = define.getGrammar().getActs().stream().anyMatch(a -> a.getType() == DynamicSQLType.SIMPLE_SAVE);
        if (hasSave) {
            return DynamicResult.fail("保存操作不支持GET访问");
        }
        try {
            paramsMap = callBefore(paramsMap, request);
            DynamicResult<?> res = dynamicLogicService.execute(define, paramsMap);
            return callAfter(res);
        } catch (Exception e) {
            LOG.error("", e);
            return DynamicResult.fail(e.getMessage());
        }
    }

    @PostMapping("/call")
    public Object call(@RequestBody @Validated @NotNull Map<String, Object> params,
            HttpServletRequest request) {
        Long dynamicId = Optional.ofNullable(params.get("dynamicId"))
                .map(String::valueOf)
                .map(Long::parseLong)
                .orElseThrow(() -> new DynamicException("dynamicId 不能为空"));
        DynamicLogicDefine define = dynamicLogicService.detail(dynamicId);
        if (define == null) {
            return DynamicResult.fail("未找到动态SQL规则");
        }
        try {
            params = callBefore(params, request);
            DynamicResult<?> res = dynamicLogicService.execute(define, params);
            return callAfter(res);
        } catch (Exception e) {
            LOG.error("", e);
            return DynamicResult.fail(e.getMessage());
        }
    }

    /**
     * call 之前需要做的额外处理
     * 
     * @param params
     * @param request
     * @return
     */
    protected abstract Map<String, Object> callBefore(Map<String, Object> params, HttpServletRequest request);

    /**
     * call 之后需要做的额外处理
     * 
     * @param res
     * @return
     */
    protected abstract Object callAfter(DynamicResult<?> res);

    // private Record getLoginUser(HttpServletRequest request) {
    // String JID = request.getHeader("Jid");
    // Optional.ofNullable(JID).orElseThrow(() -> new DynamicException("缺失Jid"));
    // Claims claims = JWTUtil.parseJWT(JID);
    // BusinessAssert.notNull(claims, "Jid解析失败");
    // String loginName = JWTUtil.getLoginNa(claims);
    // BusinessAssert.notEmpty(loginName, "Jid获取当前用户信息失败");
    // Record user = Db.findFirst("SELECT * FROM ywpz_user_s WHERE login_name=?",
    // loginName);
    // BusinessAssert.notNull(user, "用户不存在");
    // return user;
    // }

    // private void initUserParams(JSONObject data, HttpServletRequest request) {
    // Record user = getLoginUser(request);
    // // 初始化用户参数
    // user.getColumns().forEach((k, v) -> {
    // String alias = "user." + k.trim();
    // data.put(alias, v);
    // });
    // }
}
