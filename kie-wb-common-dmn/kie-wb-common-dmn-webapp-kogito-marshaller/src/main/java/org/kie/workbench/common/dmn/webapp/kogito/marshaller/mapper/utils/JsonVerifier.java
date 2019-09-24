/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.utils;

import java.util.Objects;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDefinitions;

public class JsonVerifier {

    public static void compareJSITDefinitions(JSITDefinitions original, JSITDefinitions marshalled) {
        JSONValue originalJSONValue = getJSONObject(asString(original));
        JSONValue marshalledJSONValue = getJSONObject(asString(marshalled));
        if (checkNotNull(originalJSONValue, marshalledJSONValue)) {
            compareJSONValue(originalJSONValue, marshalledJSONValue);
        } else {
            GWT.log("**************WARNING********************");
            GWT.log("originalJSONValue is  null ? " + Objects.isNull(originalJSONValue));
            GWT.log("marshalledJSONValue is  null ? " + Objects.isNull(marshalledJSONValue));
        }
    }

    private static void compareJSONValue(JSONValue original, JSONValue marshalled) {
        JSONObject originalJSONObject = original.isObject();
        JSONObject marshalledJSONObject = marshalled.isObject();
        JSONArray originalJSONArray = original.isArray();
        JSONArray marshalledJSONArray = marshalled.isArray();
        if (checkNotNull(originalJSONObject, originalJSONObject)) {
            compareJSONObject(originalJSONObject, marshalledJSONObject);
        } else if (checkNotNull(originalJSONArray, marshalledJSONArray)) {
            compareJSONArray(originalJSONArray, marshalledJSONArray);
        } else if (!original.equals(marshalled)) {
            GWT.log("**************WARNING********************");
            GWT.log("original expected : " + limitedString(original));
            GWT.log("marshalled retrieved : " + limitedString(marshalled));
        }
    }

    private static void compareJSONObject(JSONObject original, JSONObject marshalled) {
        checkKeys(original, marshalled);
        for (String originalKey : original.keySet()) {
            compareJSONObjectKey(original, marshalled, originalKey);
        }
    }

    private static void checkKeys(JSONObject original, JSONObject marshalled) {
        final Set<String> originalKeys = original.keySet();
        final Set<String> marshalledKeys = marshalled.keySet();
        if (originalKeys.size() != marshalledKeys.size()) {
            GWT.log("**************WARNING********************");
            GWT.log("original keys expected : " + originalKeys.size());
            GWT.log("marshalled keys retrieved : " + marshalledKeys.size());
        }
        for (String originalKey : originalKeys) {
            if (!marshalledKeys.contains(originalKey)) {
                GWT.log("**************WARNING********************");
                GWT.log("original key " + originalKey + " missing in marshalled " + limitedString(marshalled));
            }
        }
        for (String marshalledKey : marshalledKeys) {
            if (!originalKeys.contains(marshalledKey)) {
                GWT.log("**************WARNING********************");
                GWT.log("marshalled key " + marshalledKey + " not expected  in " + limitedString(original));
            }
        }
    }

    private static void compareJSONObjectKey(JSONObject original, JSONObject marshalled, String key) {
        final JSONValue originalJSONValue = original.get(key);
        final JSONValue marshalledJSONValue = marshalled.get(key);
        if (checkNotNull(originalJSONValue, marshalledJSONValue)) {
            compareJSONValue(originalJSONValue, marshalledJSONValue);
        } else {
            GWT.log("**************WARNING********************");
            GWT.log("original " + limitedString(original) + ":" + key + " is null ? " + Objects.isNull(originalJSONValue));
            GWT.log("marshalled " + limitedString(marshalled) + ":" + key + " is null ? " + Objects.isNull(marshalledJSONValue));
        }
    }

    private static void compareJSONArray(JSONArray original, JSONArray marshalled) {
        if (original.size() != marshalled.size()) {
            GWT.log("**************WARNING********************");
            GWT.log("original size expected " + original.size());
            GWT.log("marshalled size retrieved " + marshalled.size());
        }
        int limit = Math.min(original.size(), marshalled.size());
        for (int i = 0; i < limit; i++) {
            compareJSONValue(original.get(i), marshalled.get(i));
        }
    }

    private static boolean checkNotNull(JSONValue original, JSONValue marshalled) {
        return !Objects.isNull(original) && !Objects.isNull(marshalled);
    }

    private static JSONValue getJSONValue(String jsonString) {
        try {
            return JSONParser.parseStrict(jsonString);
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject getJSONObject(String jsonString) {
        try {
            return getJSONValue(jsonString).isObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static <D> String limitedString(D toConvert) {
        String toReturn = asString(toConvert);
        if (toReturn.length() > 100) {
            toReturn = toReturn.substring(0, 100);
        }
        return toReturn;
    }

    private static native <D> String asString(D toConvert) /*-{
        return JSON.stringify(toConvert);
    }-*/;
}
