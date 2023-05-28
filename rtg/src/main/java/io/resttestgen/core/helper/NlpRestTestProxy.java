package io.resttestgen.core.helper;

import com.google.gson.*;
import io.resttestgen.core.Environment;
import io.resttestgen.core.datatype.ParameterName;
import io.resttestgen.core.datatype.parameter.Parameter;
import io.resttestgen.core.datatype.parameter.structured.ArrayParameter;
import io.resttestgen.core.datatype.rule.Rule;
import io.resttestgen.core.datatype.rule.RuleFactory;
import io.resttestgen.core.openapi.Operation;
import kotlin.Pair;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class NlpRestTestProxy {

    private static final Logger logger = LogManager.getLogger(NlpRestTestProxy.class);
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final String baseUrl = "http://192.168.0.12:4000";
    private static final Gson gson = new Gson();
    private static final RuleFactory ruleFactory = new RuleFactory();

    private static final Map<String, String> localCache = new HashMap<>();
    private static int serverCount = 0;
    private static int cacheCount = 0;

    @NotNull
    public static HashSet<Rule> extractRulesFromParameterText(Parameter parameter) {

        HashSet<Rule> rules = new HashSet<>();

        // Continues only if description has at least 3 chars
        if (parameter.getDescription().trim().length() > 3) {

            Map<String, String> jsonMap = new TreeMap<>();
            jsonMap.put("requestParameters", getParameterNamesList(parameter.getOperation()));
            jsonMap.put("target", parameter.getName().toString());
            jsonMap.put("responseParameters", getResponseParameterNamesList(parameter.getOperation()));
            jsonMap.put("description", parameter.getDescription());

            String jsonBody = gson.toJson(jsonMap);

            String responseBody = localCache.get(jsonBody);

            if (responseBody == null) {
                serverCount++;
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request request = new Request.Builder().url(baseUrl + "/parameter").post(body).build();
                Call call = httpClient.newCall(request);
                try {
                    Response response = call.execute();
                    responseBody = response.body().string();
                    localCache.put(jsonBody, responseBody);
                    System.out.println("[P] --- " + parameter.getDescription() + " ---> " + responseBody);
                } catch (IOException ignored) {
                }
            } else {
                cacheCount++;
            }

            if (responseBody != null) {
                for (Pair<ParameterName, Pair<String, String>> rulePair : normalizeRules(responseBody)) {
                    Rule rule = ruleFactory.createRule(rulePair.getFirst(), rulePair.getSecond());
                    if (rule != null) {
                        rules.add(rule);
                    }
                }
            }
        }

        return rules;
    }

    @NotNull
    public static HashSet<Rule> extractRulesFromOperationText(Operation operation) {

        HashSet<Rule> rules = new HashSet<>();

        // Continues only if operation has request parameters and description has at least 3 chars
        if (operation.getAllRequestParameters().size() > 0 && operation.getDescription().trim().length() > 3) {

            Map<String, String> jsonMap = new TreeMap<>();
            jsonMap.put("requestParameters", getParameterNamesList(operation));
            jsonMap.put("responseParameters", getResponseParameterNamesList(operation));
            jsonMap.put("description", operation.getDescription());

            String jsonBody = gson.toJson(jsonMap);

            String responseBody = localCache.get(jsonBody);

            if (responseBody == null) {
                serverCount++;
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request request = new Request.Builder().url(baseUrl + "/operation").post(body).build();
                Call call = httpClient.newCall(request);
                try {
                    Response response = call.execute();
                    responseBody = response.body().string();
                    localCache.put(jsonBody, responseBody);
                    System.out.println("[O] --- " + operation.getDescription() + " ---> " + responseBody);

                } catch (JsonSyntaxException e) {
                    logger.warn("Could not parse JSON body of response.");
                } catch (IOException ignored) {
                }
            } else {
                cacheCount++;
            }

            if (responseBody != null) {
                for (Pair<ParameterName, Pair<String, String>> rulePair : normalizeRules(responseBody)) {
                    Rule rule = ruleFactory.createRule(rulePair.getFirst(), rulePair.getSecond());
                    if (rule != null) {
                        rules.add(rule);
                    }
                }
            }
        }

        return rules;
    }

    @NotNull
    public static HashSet<Rule> extractRulesFromRequestBodyDescription(Operation operation) {

        HashSet<Rule> rules = new HashSet<>();

        // Continues only if request body is defined and description has at least 3 chars
        if (operation.getRequestBody() != null && operation.getRequestBodyDescription().trim().length() > 3) {

            Map<String, String> jsonMap = new TreeMap<>();
            jsonMap.put("requestParameters", getParameterNamesList(operation));
            jsonMap.put("target", operation.getRequestBody().getName().toString());
            jsonMap.put("responseParameters", getResponseParameterNamesList(operation));
            jsonMap.put("description", operation.getRequestBodyDescription());

            String jsonBody = gson.toJson(jsonMap);

            String responseBody = localCache.get(jsonBody);

            if (responseBody == null) {
                serverCount++;
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request request = new Request.Builder().url(baseUrl + "/parameter").post(body).build();
                Call call = httpClient.newCall(request);
                try {
                    Response response = call.execute();
                    responseBody = response.body().string();
                    localCache.put(jsonBody, responseBody);
                    System.out.println("[R] --- " + operation.getRequestBodyDescription() + " ---> " + responseBody);
                } catch (IOException ignored) {
                }
            } else {
                cacheCount++;
            }

            if (responseBody != null) {
                for (Pair<ParameterName, Pair<String, String>> rulePair : normalizeRules(responseBody)) {
                    Rule rule = ruleFactory.createRule(rulePair.getFirst(), rulePair.getSecond());
                    if (rule != null) {
                        rules.add(rule);
                    }
                }
            }
        }

        return rules;
    }

    @NotNull
    public static HashSet<Rule> extractRulesFromServerMessage(Operation operation, String serverMessage) {

        HashSet<Rule> rules = new HashSet<>();

        // Continues only if operation has request parameters and description has at least 3 chars
        if (operation.getAllRequestParameters().size() > 0 && serverMessage.trim().length() > 3) {

            Map<String, String> jsonMap = new TreeMap<>();
            jsonMap.put("requestParameters", getParameterNamesList(operation));
            jsonMap.put("executedParameters", getUsedRequestParameterNamesList(operation));
            jsonMap.put("responseParameters", getResponseParameterNamesList(operation));
            jsonMap.put("description", serverMessage);

            String jsonBody = gson.toJson(jsonMap);

            String responseBody = localCache.get(jsonBody);

            if (responseBody == null) {
                serverCount++;
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request request = new Request.Builder().url(baseUrl + "/message").post(body).build();
                Call call = httpClient.newCall(request);
                try {
                    Response response = call.execute();
                    responseBody = response.body().string();
                    localCache.put(jsonBody, responseBody);
                    System.out.println("[S] --- " + serverMessage + " ---> " + responseBody);
                } catch (JsonSyntaxException e) {
                    logger.warn("Could not parse JSON body of response.");
                } catch (IOException ignored) {
                }
            } else {
                cacheCount++;
            }

            if (responseBody != null) {
                for (Pair<ParameterName, Pair<String, String>> rulePair : normalizeRules(responseBody)) {
                    Rule rule = ruleFactory.createRule(rulePair.getFirst(), rulePair.getSecond());
                    if (rule != null) {
                        rules.add(rule);
                    }
                }
            }
        }

        return rules;
    }

    private static String getParameterNamesList(Operation operation) {

        // TODO: understand why parameter are removed from operation. Temporary fix is to retrieve original operation
        final Operation finalOperation = operation;
        Optional<Operation> originalOperation = Environment.getInstance().getOpenAPI().getOperations().stream()
                .filter(o -> o.equals(finalOperation)).findFirst();
        if (originalOperation.isPresent()) {
            operation = originalOperation.get();
        }

        List<String> parameterNames = operation.getAllRequestParameters().stream()
                .filter(e -> !(e.getParent() instanceof ArrayParameter))
                .map(e -> e.getName().toString())
                .distinct().sorted()
                .collect(Collectors.toList());
        parameterNames.remove("");
        StringJoiner stringJoiner = new StringJoiner(",");
        parameterNames.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    private static String getResponseParameterNamesList(Operation operation) {
        StringJoiner stringJoiner = new StringJoiner(",");
        if (operation.getSuccessfulOutputParameters() != null) {
            List<String> parameterNames = operation.getSuccessfulOutputParameters().getAllParameters().stream()
                    .filter(e -> !(e.getParent() instanceof ArrayParameter) && !e.getName().toString().trim().equals(""))
                    .map(e -> e.getName().toString())
                    .distinct().sorted()
                    .collect(Collectors.toList());
            parameterNames.forEach(stringJoiner::add);
        }
        return stringJoiner.toString();
    }

    private static String getUsedRequestParameterNamesList(Operation operation) {
        StringJoiner stringJoiner = new StringJoiner(",");
        List<String> parameterNames = operation.getAllRequestParameters().stream()
                .filter(e -> e.getValue() != null && !(e.getParent() instanceof ArrayParameter) && !e.getName().toString().trim().equals(""))
                .map(e -> e.getName().toString())
                .distinct().sorted()
                .collect(Collectors.toList());
        parameterNames.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    private static Collection<Pair<ParameterName, Pair<String, String>>> normalizeRules(String rulesJson) {
        LinkedList<Pair<ParameterName, Pair<String, String>>> rules = new LinkedList<>();
        try {
            JsonObject parsedJson = gson.fromJson(rulesJson, JsonObject.class);
            for (String parameterName : parsedJson.keySet()) {
                JsonObject object = parsedJson.getAsJsonObject(parameterName);
                for (String keyword : object.keySet()) {
                    JsonArray array = object.getAsJsonArray(keyword);
                    for (JsonElement primitive : array) {
                        rules.add(new Pair<>(new ParameterName(parameterName), new Pair<>(keyword, primitive.getAsString())));
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Could not parse JSON.");
        }
        return rules;
    }

    public static void printStatistics() {
        System.out.println("STATISTICS: " + serverCount + "/" + cacheCount + " (server/cache)");
    }
}
