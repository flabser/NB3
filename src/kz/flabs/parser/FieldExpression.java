package kz.flabs.parser;

import kz.flabs.webrule.constants.FieldType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldExpression {
    public String openingElement = "";
    public String fieldName;
    public String operand;
    public String fieldValue;
    public String closingElement = "";
    public String closingBracket = "";
    //public String condition = "";
    public boolean valueIsQuoted;
    public boolean isSuggestionRequest;
    public boolean isRegexRequest;
    public boolean paramatrized;
    public String parameterName;
    public FieldType fieldType = FieldType.TEXT;

    private static final Pattern splitConditionOperands =
            Pattern.compile("(\\(*)(\\S*)\\s*(<=|>=|<>|!=|=|<|>|~\\*|~|\\s+match\\s+|\\s+in\\s+|\\s+not in\\s+|\\s+is\\s+){1}\\s*([^\f\n\r\t]+?)(\\s+&|\\s+and|\\s+or|\\s*\\)$|\\s*\\Z|\\Z)");
    private static final Pattern quotedPattern = Pattern.compile("^('|\"){1}(.*?)('|\"){1}\\s*(\\s*&|\\s*and|\\s*or|\\Z|\\s*\\)|\\s*\\Z)$");
    private static final Pattern unQuotedPattern = Pattern.compile("^\\s*(\\d+)\\s*(\\s*&|\\s*and|\\s*or|\\Z|\\s*\\Z)$");
    private static final Pattern fieldTypePattern = Pattern.compile("^\\s*(\\S*)#(\\S*)\\s*$");
    private static final Pattern p = Pattern.compile("^('|\\()?\\$(\\S+[^('|\\)])");

    FieldExpression(String text) {
        //text = text.replaceAll("\\s*","");
        Matcher matcher = splitConditionOperands.matcher(text);
        if (matcher.find()) {
            openingElement = matcher.group(1);
            fieldName = matcher.group(2);
            operand = matcher.group(3).trim();
            if (operand.equalsIgnoreCase("~")) {
                isSuggestionRequest = true;
            }
            if (operand.equalsIgnoreCase("match")) {
                isRegexRequest = true;
            }
            String fullFieldValue = matcher.group(4);
            closingElement = matcher.group(5).trim().replace("&", "AND") + " ";

            Matcher quotedMatcher = quotedPattern.matcher(fullFieldValue);
            if (quotedMatcher.find()) {
                valueIsQuoted = true;
                closingBracket = quotedMatcher.group(4);
                fieldValue = quotedMatcher.group(2).replace("'", "''");

                Matcher fieldTypeMatcher = fieldTypePattern.matcher(fieldName);
                if (fieldTypeMatcher.find()) {
                    fieldName = fieldTypeMatcher.group(1);
                    String suffix = fieldTypeMatcher.group(2);
                    if (suffix.equalsIgnoreCase("number")) {
                        fieldType = FieldType.NUMBER;
                    } else if (suffix.equalsIgnoreCase("glossary")) {
                        fieldType = FieldType.GLOSSARY;
                    } else if (suffix.equalsIgnoreCase("datetime")) {
                        fieldType = FieldType.DATETIME;
                    }
                }
            } else {
                Matcher unQuotedMatcher = unQuotedPattern.matcher(fullFieldValue);
                if (unQuotedMatcher.find()) {
                    fieldValue = unQuotedMatcher.group(1);
                } else {
                    fieldValue = fullFieldValue;
                }

                Matcher fieldTypeMatcher = fieldTypePattern.matcher(fieldName);
                if (fieldTypeMatcher.find()) {
                    fieldName = fieldTypeMatcher.group(1);
                    String suffix = fieldTypeMatcher.group(2);
                    if (suffix.equalsIgnoreCase("number")) {
                        fieldType = FieldType.NUMBER;
                    } else if (suffix.equalsIgnoreCase("glossary")) {
                        fieldType = FieldType.GLOSSARY;
                    } else if (suffix.equalsIgnoreCase("datetime")) {
                        fieldType = FieldType.DATETIME;
                    } else {
                        fieldType = FieldType.NUMBER;
                    }
                }
            }
            Matcher parFieldMatcher = p.matcher(fullFieldValue);
            if (parFieldMatcher.find()) {
                paramatrized = true;
                parameterName = (parFieldMatcher.group(2));
            }
        }
    }

    public String getContent() {
        String formula = openingElement;
        if (valueIsQuoted) {
            if (isSuggestionRequest) {
                if (fieldType != FieldType.DATETIME && !fieldName.contains("date")) {
                    formula += fieldName + " LIKE '%" + fieldValue + "%' ";
                } else {
                    formula += "cast (" + fieldName + " as text) LIKE '%" + fieldValue + "%' ";
                }
            } else if (isRegexRequest) {
                formula += fieldName + " ~ '" + fieldValue + "' ";
            } else {
                formula += fieldName + operand + " '" + fieldValue + "' ";
            }
        } else {
            formula += fieldName + " " + operand + " " + fieldValue + " ";
        }
        return formula + closingBracket + closingElement;
    }

    public String toString() {
        return openingElement + "|" + fieldName + "|" + operand + "|" + fieldValue + "|" + closingElement + ", isQuoted=" + valueIsQuoted;
    }

}
