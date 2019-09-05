package com.github.shicloud.bridge.model;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.shicloud.bridge.Startup;
import com.github.shicloud.jdbc.annotation.ID;
import com.github.shicloud.utils.CamelNameUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

public class ModelParser {
	private static final Logger log = LoggerFactory.getLogger(ModelParser.class);

	private static final String MODEL_PACKAGE = "com.github.shicloud.bridge.pojo.";
	public static ClassPool POOL = ClassPool.getDefault();

	public static List<Model> parser(String json) {
		List<Model> models = JSON.parseArray(json, Model.class);
		return models;
	}

	public static void loadAll(List<Model> models) throws Exception {
		for (Model model : models) {
			String upperName = upperFirstStr(model.getName());
			String className = MODEL_PACKAGE + upperName;
			CtClass ctClass = POOL.makeClass(className);
			ClassFile ccFile = ctClass.getClassFile();
			ConstPool constPool = ccFile.getConstPool();

			if (model.getTableName() != null) {
				AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
				Annotation table = new Annotation("javax.persistence.Table", constPool);
				table.addMemberValue("name", new StringMemberValue(model.getTableName(), constPool));
				attr.addAnnotation(table);
				ccFile.addAttribute(attr);
			}

			for (Field field : model.getFields()) {
				try {
					createField(ctClass, field);
				} catch (CannotCompileException | NotFoundException e) {
					log.error(upperName + " class create field {} error", field.getName(), e);
				}
			}

			FileUtils.writeByteArrayToFile(
					new File(Startup.class.getResource("/").getPath() + "/" + className.replace(".", "/") + ".class"),
					ctClass.toBytecode());
			Class<?> c = Class.forName(className);

			model.setClazz(c);
		}
	}

@Transient
	private static void createField(CtClass ctClass, Field field) throws CannotCompileException, NotFoundException {
		CtField f = new CtField(getType(field.getType()),field.getName(),ctClass);
		f.setModifiers(Modifier.PRIVATE);
		ctClass.addField(f);
		String upperFieldStr = upperFirstStr(field.getName());
		ctClass.addMethod(CtNewMethod.getter("get"+upperFieldStr, f));
		ctClass.addMethod(CtNewMethod.setter("set"+upperFieldStr, f));
		
		ClassFile ccFile = ctClass.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
		
		AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation parser = new Annotation("com.github.shicloud.bytes.annotation.Parser", constPool);
        parser.addMemberValue("index", new IntegerMemberValue(constPool,field.getIndex()!=null?field.getIndex():0));
        parser.addMemberValue("lenght", new IntegerMemberValue(constPool,field.getLenght()!=null?field.getLenght():0));
        parser.addMemberValue("offset", new IntegerMemberValue(constPool,field.getOffset()!=null?field.getOffset():0));
        parser.addMemberValue("dependsOn", new IntegerMemberValue(constPool,field.getDependsOn()!=null?field.getDependsOn():0));
        attr.addAnnotation(parser);
		
		if(field.getIsTransient()!=null && field.getIsTransient()) {
			Annotation transientAttr = new Annotation("javax.persistence.Transient", constPool);
	        attr.addAnnotation(transientAttr);
		}
		if(field.getIdType()!=null) {
			Annotation idAttr = new Annotation("com.github.shicloud.jdbc.annotation.ID", constPool);
			EnumMemberValue enumValue = new EnumMemberValue(constPool);
			enumValue.setType(ID.TYPE.class.getName());
			enumValue.setValue("AUTO".equals(field.getIdType().toUpperCase())?ID.TYPE.AUTO.name():ID.TYPE.INPUT.name());
			idAttr.addMemberValue("value", enumValue);
			idAttr.addMemberValue("column", new StringMemberValue(CamelNameUtils.camel2underscore(field.getName()),constPool));
	        attr.addAnnotation(idAttr);
		}
		
		f.getFieldInfo().addAttribute(attr);
	}

	private static CtClass getType(String type) throws NotFoundException {
		String t = type.toLowerCase();
		if ("int".equals(t)) {
			t = "java.lang.Integer";
		} else if ("date".equals(t)) {
			t = "java.util.Date";
		} else if ("byte[]".equals(t)) {
			t = byte[].class.getTypeName();
		} else {
			t = "java.lang." + upperFirstStr(t);
		}
		return POOL.getCtClass(t);
	}

	private static String upperFirstStr(String str) {
		String namePart1 = str.substring(0, 1).toUpperCase();
		String namePart2 = str.substring(1);
		StringBuffer upperName = new StringBuffer().append(namePart1 + namePart2);
		return upperName.toString();
	}
}
