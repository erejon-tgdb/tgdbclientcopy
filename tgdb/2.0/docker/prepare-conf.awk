#!/usr/local/bin/awk -f
#
# Copyright (c) 2018 TIBCO Software Inc.
# All rights reserved.
#
# File name : prepare-conf.awk
# Created on: Oct 14, 2017
# Created by: achavan
#
# This is the AWK script to build a Docker Image for TIBCO Graph Database
#
# Arguments:
#    ARGV[1] = Configuration Template File e.g. initdb.template OR tgdb.template
#    ARGV[2] = Default Configuration File e.g. tgdb-default.txt
#    ARGV[3] = Variables / Tokens File w/ user-defined substitution values e.g. variables.txt
#
# Approach:
#    <1> Read the template file and prepare an array/map for all the tokens that are present in the template file.
#    <2> Read the defaults file and substitute/in-place-replace the default value against the entry in the map.
#    <3> Read the user-defined variables file and override the values of tokens as an in-place-replacement in the map.
#    <4> Create a configuration file from the latest consolidated/overridden values of all configuration parameters.
#
# Output:
#    Properly generated configuration file combining all the inputs from above 3 files e.g. initdb.conf OR tgdb.conf
#       
# Usage:
#    awk -f prepare-conf.awk <>.template tgdb-default.txt variables.txt ==> will result in '<>.conf'
#    e.g. awk -f prepare-conf.awk initdb.template tgdb-default.txt variables.txt ==> will result in 'initdb.conf'
#    e.g. awk -f prepare-conf.awk tgdb.template tgdb-default.txt variables.txt ==> will result in 'tgdb.conf'
#       
# This can be invoked by the build process after building the product deliverables.
#

function ltrim(s) { sub(/^[ \t\r\n]+/, "", s); return s; }
function rtrim(s) { sub(/[ \t\r\n]+$/, "", s); return s; }
function trim(s)  { return rtrim(ltrim(s)); }
function isNumber(s) { return match(s, "^[0-9]+$"); }
function toHex(s) {  sub(/^[0]+/,"0x", s); return s; }

function isConfigurationComment(s) { return match(s, "^#.*$"); }
function isEmpty(s) { return match(s, "^[ \t]*$"); }
function isSection(s) { return match(s, "^\\["); }
function isTokenized(s) { return match(s, "\\{.*\\}"); }
function isInlineComment(s) { return match(s, "//.*$"); }
function isDefaultsFile(s) { return match(s, "default"); }
function isTemplateFile(s) { return match(s, "template"); }
function isUserDefinedVariableFile(s) { return match(s, "variable"); }

function baseName(file) {
	sub(".*/", "", file)
	return file
}

function formatOutputFileName(s) {
	if (isTemplateFile(s)) {
		fileName = baseName(s)
		elementCount = split(fileName, fileElements, ".");
#		if (elementCount == 3) {
#			return sprintf("%s.%s", fileElements[1], fileElements[3]);
			return sprintf("%s.%s", fileElements[1], "conf");
#		}
	}
}

function populateSubstitutionMap(d,s1,s2,s3,s4,s5) {
	lineCount = 0;
	if (isNumber(d)) { 
		lineCount = strtonum(d); 
		outputFields[lineCount,1] = s1;
		outputFields[lineCount,2] = s2;
		outputFields[lineCount,3] = s3;
		outputFields[lineCount,4] = s4;
#		printf("%d :: %s\n", lineCount, s5);
#		printf("<==> OutputFields[%d] %s\t%s\t%s\t%s\n", lineCount, outputFields[lineCount,1], outputFields[lineCount,2], outputFields[lineCount,3], outputFields[lineCount,4]);
	}
	return;
}

### Tokenize the line containing substitution variable into an array
### First element of array is 'Name of the configuration parameter'
### Second element of array is '='
### Third element of array is 'Formatted Substituion Token'
### Fourth element of array is 'Inline comment starting with // character'
function parseLineForTokenSubstitution(s, p) {
	fields[""] = "";
	tokenFields[""] = "";i
	
	### Separate Inline Comments
	splitCount = split(s, fields, "//")
	tokenizedString = fields[1];
	inlineComment = sprintf("//%s", trim(fields[2]));
#	printf("\t\tTokenized String: %s <<==>> Inline Comment: %s\n", tokenizedString, inlineComment);
	
#	fieldCount = split(tokenizedString, tokenFields, "[ \t]+|[ \t]*");
	fieldCount = split(tokenizedString, tokenFields, p);
#	printf("\t\tNo of fields: %d\n", fieldCount);
	gsub(/\$\{/, "", tokenFields[3]);
	gsub(/\}/, "", tokenFields[3]);
	tokenFields[4] = inlineComment
	if (tokenFields[4] == "") {
		return sprintf("%s~%s~%s\n", tokenFields[1], tokenFields[2], tokenFields[3]);
	} else if ((tokenFields[3] == "") && (tokenFields[4] == "")) {
		return sprintf("%s~%s\n", tokenFields[1], tokenFields[2]);
	}
	return sprintf("%s~%s~%s~%s\n", tokenFields[1], tokenFields[2], tokenFields[3], tokenFields[4]);
}

### For every line input passed in via argument, loop through configuration parameter/value pair map generated
### in earlier step while reading the '*.template.conf' file to match the entries.
### If the input line has the same configuration parameter entity, extract the default value from the input line
### and set it for the configuration parameter entry within the map - as an in-place replacement.
function substituteTokensWithDefaults(s) {
	fields[""] = "";
	tokenFields[""] = "";
	
#	printf("    ==> SubstToken::Input::Line[%d]: %s\n", (NR-templateLineCount), s);
	reformattedLine = parseLineForTokenSubstitution(s, "[ \t]+|[ \t]*");
	noOfFields = split(reformattedLine, tokenFields, "~");
#	printf("    ==> SubstToken::InputFields::Line[%d]: %s\t%s\t%s\t%s\n", (NR-templateLineCount), tokenFields[1], tokenFields[2], tokenFields[3], tokenFields[4]);
	### Loop through the substitution map
	for (i=1; i<=templateLineCount; i++) {
		name = outputFields[i,1];
		sign = outputFields[i,2];
		value = outputFields[i,3];
		comment = outputFields[i,4];
#		printf("    ==> SubstToken::FromOutputFields::Line[%d]: Name: %s\tSign: %s\tValue: %s\tComment: %s\n", i, name, sign, value, comment);
		if (tokenFields[1] == name) {
			### First Token i.e. parameter name matches
			modifiedValue = sprintf("%s<>%s", value, tokenFields[3]);
			outputFields[i,3] = modifiedValue;
#			printf("    ==> SubstToken::ModifiedValue::Line[%d]: Name: %s\tSign: %s\tValue: %s\tComment: %s\n", i, name, sign, modifiedValue, comment);
			break;
		}
	}
	fields[""] = "";
	tokenFields[""] = "";
	return
}

### For every line passed in via argument, it will in the format 'name=value'.
### Parse the line to extract parameter name and parameter value.
m## Loop through the substitution map to find out the entry that has parameter name as the key
### and override its value with whatever is read from the incoming argument - as an in-place replacement.
function setUserDefinedParameterValues(s) {
	fields[""] = "";
	
#	printf("    ==> SetUserDefined::Input::Line[%d]: %s\n", (NR-templateLineCount), s);
	reformattedLine = parseLineForTokenSubstitution(s, "=");
	noOfFields = split(reformattedLine, tokenFields, "~");
#	printf("    ==> SetUserDefined::InputFields::Line[%d]: %s\t%s\n", (NR-templateLineCount), tokenFields[1], tokenFields[2]);
	### Loop through the substitution map
	for (i=1; i<=templateLineCount; i++) {
		name = outputFields[i,1];
		sign = outputFields[i,2];
		value = outputFields[i,3];
		comment = outputFields[i,4];
#		printf("    ==> SetUserDefined::FromOutputFields::Line[%d]: Name: %s\tSign: %s\tValue: %s\tComment: %s\n", i, name, sign, value, comment);
		if (match(value, tokenFields[1])) {
			### Token i.e. parameter name matches is present in values collected in outputFields
			modifiedValue = sprintf("%s<>%s", value, tokenFields[2]);
			outputFields[i,3] = modifiedValue;
#			printf("    ==> SubstToken::ModifiedValue::Line[%d]: Name: %s\tSign: %s\tValue: %s\tComment: %s\n", i, name, sign, modifiedValue, comment);
			break;
		}
	}
	fields[""] = "";
	return;
}

BEGIN {
	FS = " ";
#	FS = ";|[ \t]*|[ \t]+";
	templateLineCount = 0;
	outputFile = "";
	outputFields[""] = "";
}
{
	line = $0

	### Read the TEMPLATE file and prepare necessary substitution maps for all the token variables.
	if (FNR == NR) { # This will be true for the first file
		### Formulate Output File Name
		outputFile = formatOutputFileName(ARGV[1]);
		++templateLineCount;

		### If the input line is either EMPTY or SECTION declaration, do not parse
		if (isEmpty(line) || isSection(line)) {
			populateSubstitutionMap(templateLineCount, trim(line), "", "", "", line);

		### If the input line is Configuration Comment, do not parse if it does not have inline comment starting with ';'
		} else if (isConfigurationComment(line)) {
			if (isInlineComment(line)) {
				reformattedLine = parseLineForTokenSubstitution(line, "[ \t]+|[ \t]*");
				noOfFields = split(reformattedLine, tokenFields, "~");
				populateSubstitutionMap(templateLineCount, tokenFields[1], tokenFields[2], tokenFields[3], tokenFields[4], reformattedLine);
			} else {
				populateSubstitutionMap(templateLineCount, trim(line), "", "", "", line);
			}

		### If the input line has substitution tokens present in the form of '${...}', parse into individual element set
		} else if (isTokenized(line)) {
			reformattedLine = parseLineForTokenSubstitution(line, "[ \t]+|[ \t]*");
			noOfFields = split(reformattedLine, tokenFields, "~");
			populateSubstitutionMap(templateLineCount, tokenFields[1], tokenFields[2], tokenFields[3], tokenFields[4], reformattedLine);

		### If the input line has an other types of option schema comments starting with '//', do not parse
		} else {
			if (isInlineComment(line)) {
				reformattedLine = parseLineForTokenSubstitution(line, "[ \t]+|[ \t]*");
				noOfFields = split(reformattedLine, tokenFields, "~");
				populateSubstitutionMap(templateLineCount, tokenFields[1], tokenFields[2], tokenFields[3], tokenFields[4], reformattedLine);
			} else {
				populateSubstitutionMap(templateLineCount, trim(line), "", "", "", line);
			}
		}
	}
	
	### Read the DEFAULTS file to extract and set the default values for ONLY those configuration parameters that are present in substitution map.
	if (isDefaultsFile(FILENAME)) { 
		if (isEmpty(line) || isSection(line)) {
			next;	### Skip the rest
		} else if (isConfigurationComment(line)) {
			if (isInlineComment(line)) {
				substituteTokensWithDefaults(line);
			} else {
				next;	### Skip the rest
			}
		} else {
#			substituteTokensWithDefaults(line);
		}
	}
	
	### Read the User-Defined-Variables file to extract the user defined values of parameter tokens and set them in the substitution map.
	if (isUserDefinedVariableFile(FILENAME)) { 
		if (isConfigurationComment(line) || isEmpty(line) || isSection(line)) {
			next;	### Skip the rest
		} else {
			setUserDefinedParameterValues(line);
		}
	}
}
END {
	# Write a properly substituted configuration file
#	printf("Output File: %s\n", outputFile);
#	printf("Output File: %s\n", outputFile) > outputFile;
#	printf("No of formatted lines: %d\n", templateLineCount);
	for (i=1; i<=templateLineCount; i++) {
		name = trim(outputFields[i,1]);
		sign = trim(outputFields[i,2]);
		value = trim(outputFields[i,3]);
		noOfValues = split(value, values, "<>");
		if (noOfValues == 2) {
			value = values[2];
		} else if (noOfValues == 3) { 
			value = values[3]; 
		} else if (noOfValues == 4) {
			value = values[4]; 
		} else if (noOfValues == 5) {
			value = values[5]; 
		}
		comment = trim(outputFields[i,4]);
#		printf("==> Line[%d]: Name: %s\tSign: %s\tValue: %s\tComment: %s\n", i, name, sign, recentValue, comment);
		printf("%s %s %s \t%s\n", name, sign, value, comment) >> outputFile;
#		printf("%s %s %s \t%s\n", name, sign, recentValue, comment);
	}
}
