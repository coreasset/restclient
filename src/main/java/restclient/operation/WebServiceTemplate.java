package restclient.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import restclient.ApiConfigInitializingException;
import restclient.meta.HttpMethod;
import restclient.model.WebServiceParam;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by chanwook on 2014. 6. 19..
 */
public class WebServiceTemplate {
    private final Logger logger = LoggerFactory.getLogger(WebServiceTemplate.class);

    private final RestTemplate springTemplate;

    public WebServiceTemplate() {
        springTemplate = new RestTemplate();
    }

    public WebServiceTemplate(RestTemplate springTemplate) {
        this.springTemplate = springTemplate;
    }


    public Object execute(WebServiceParam param) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(headers);

        String apiUrl = createApiUrl(param);
        org.springframework.http.HttpMethod method = convertSpringMethod(param.getMethod());
        Map<String, Object> pathParam = createPathParam(param.getUrl(), param.getArguments(), param.getNamedPathMap());

        if (logger.isDebugEnabled()) {
            logger.debug("===============================================================================");
            logger.debug("API 호출 상세 정보[url: " + apiUrl + ", method: " + method.name() + ", arguments: " + param.getArguments() +
                    ", returnType: " + param.getReturnType() + "]");
            logger.debug("===============================================================================");
        }

        ResponseEntity<?> responseEntity = springTemplate.exchange(apiUrl, method, httpEntity, param.getReturnType(), pathParam);

        return responseEntity.getBody();
    }

    protected Map<String, Object> createPathParam(String url, Object[] arguments, Map<String, String> namedPathMap) {
        Map<String, Object> pathParam = new HashMap<String, Object>();
        String[] vars = url.split("/");
        for (String v : vars) {
            if (v.startsWith("{") && v.endsWith("}")) {
                String key = v.replace("{", "").replace("}", "");
                if (Pattern.matches("\\d", key)) {
                    int index = Integer.parseInt(key);
                    if (0 == index) {
                        throw new IllegalArgumentException("Path Variable의 index는 1부터 시작으로 '0' 값을 줄 수 없습니다!");
                    } else if (index > arguments.length) {
                        throw new IllegalArgumentException(key + "에 해당하는 순번의 파라미터 인자 값이 전달되지 않았습니다!(index: " + key + ", 파라미터 크기: " + arguments.length + ")");
                    }
                    Object value = arguments[index - 1];
                    pathParam.put(key, value);
                } else/* named path 지원 */ {
                    if (namedPathMap.containsKey(key)) {
                        int paramIndex = Integer.parseInt(namedPathMap.get(key));
                        if (paramIndex > arguments.length) {
                            throw new IllegalArgumentException(key + "에 해당하는 순번의 파라미터 인자 값이 전달되지 않았습니다!(key: " + key + ", index: " + paramIndex + ", 파라미터 크기: " + arguments.length + ")");
                        }
                        pathParam.put(key, arguments[paramIndex - 1]);
                    } else {
                        throw new IllegalArgumentException(key + "에 해당하는 Named Path 값이 없습니다. @Path 애노테이션을 적절히 사용했는지 확인하세요!");
                    }
                }
            }
        }
        return pathParam;
    }

    private String createApiUrl(WebServiceParam param) {
        if (!StringUtils.hasText(param.getUrl()) || !StringUtils.hasText(param.getHostUrl())) {
            throw new ApiConfigInitializingException("API URL 정보가 잘 못됐습니다!!(HOST URL: " + param.getHostUrl() + ", Service URL: " + param.getUrl());
        }
        if (param.getUrl().startsWith("/")) {
            return param.getHostUrl() + param.getUrl().substring(1);
        }
        return param.getHostUrl() + param.getUrl();
    }

    private org.springframework.http.HttpMethod convertSpringMethod(HttpMethod method) {
        if (method == null) {
            throw new ApiConfigInitializingException("Method Annotation을 반드시 웹서비스 메서드에 지정해야 합니다!");
        }
        if (method.compareTo(HttpMethod.GET) == 0) return org.springframework.http.HttpMethod.GET;
        return null;
    }

    public RestTemplate getSpringTemplate() {
        return springTemplate;
    }
}
