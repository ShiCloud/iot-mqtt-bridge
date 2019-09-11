package com.github.shicloud.bridge.model;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
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
	private static final String REF_VALUE_PACKAGE = "com.github.shicloud.bridge.annotation.RefValue";
	private static final String STATIC_VALUE_PACKAGE = "com.github.shicloud.bridge.annotation.StaticValue";
	private static final String MODEL_PACKAGE = "com.github.shicloud.bridge.pojo.";
	private static final String TABLE_PACKAGE = "javax.persistence.Table";
	private static final String TRANSIENT_PACKAGE = "javax.persistence.Transient";
	private static final String ANNOTATION_ID_PACKAGE = "com.github.shicloud.jdbc.annotation.ID";
	private static final String PARSER_PACKAGE = "com.github.shicloud.bytes.annotation.Parser";
	private static final String TARGET_MODEL_PACKAGE = "com.github.shicloud.bytes.annotation.TargetModel";
	private static final String LITTLE_END_PACKAGE = "com.github.shicloud.bytes.annotation.LittleEnd";

	private static final Logger log = LoggerFactory.getLogger(ModelParser.class);

	public static ClassPool POOL = ClassPool.getDefault();

	private static Map<String, Model> modelMap = new HashMap<>();
	private static Map<String, Field> staticFieldMap = new HashMap<>();
	
	public static Map<String, Model> getModelMap() {
		return modelMap;
	}

	public static Map<String, Field> getStaticFieldMap() {
		return staticFieldMap;
	}

	public static Map<String, Model> loadAll(String json) {
		List<Model> models = JSON.parseArray(json, Model.class);
		for (Model model : models) {
			String name = model.getName();
			if (StringUtils.isEmpty(name)) {
				log.error("model name is empty, please set it ");
				System.exit(-1);
			}
			modelMap.put(model.getName(), model);
		}
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		List<ForkJoinTask<?>> tasks = new ArrayList<>();
		for (Model model : modelMap.values()) {
			ForkJoinTask<?> task = forkJoinPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						createClass(model);
					} catch (Exception e) {
						log.error("create class "+model.getName()+" failure ",e);
						System.exit(-1);
					}
				}
			});
			tasks.add(task);
		}
		try {
			for (ForkJoinTask<?> forkJoinTask : tasks) {
				forkJoinTask.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			log.error("loadAll modle failure ",e);
			System.exit(-1);
		}
		return modelMap;
	}
	
	
	private static void createClass(Model model) throws Exception {
		String name = model.getName();
		String upperName = upperFirstStr(name);
		String className = MODEL_PACKAGE + upperName;
		CtClass ctClass = POOL.makeClass(className);
		ClassFile ccFile = ctClass.getClassFile();
		ConstPool constPool = ccFile.getConstPool();

		if (model.getTableName() != null) {
			AnnotationsAttribute attr = new AnnotationsAttribute(constPool,
					AnnotationsAttribute.visibleTag);
			Annotation table = new Annotation(TABLE_PACKAGE, constPool);
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

		String classpath = ResourceUtils.getURL("classpath:").getPath();
		log.debug("classpath::" + classpath);
		FileUtils.writeByteArrayToFile(new File(classpath + className.replace(".", "/") + ".class"),
				ctClass.toBytecode());
		Class<?> c = Class.forName(className);

		model.setClazz(c);
	} 

	private static void createField(CtClass ctClass, Field field) 
			throws CannotCompileException, NotFoundException, InterruptedException, ClassNotFoundException {
		String name = field.getName();
		if (StringUtils.isEmpty(name)) {
			log.error("field name is empty, please set it ");
			System.exit(-1);
		}
		
		ClassFile ccFile = ctClass.getClassFile();
		ConstPool constPool = ccFile.getConstPool();
		Annotation staticValueAnno = null;
		if (name.startsWith("@")) {
			name = name.replace("@", "");
			int lastIndexOf = ctClass.getName().lastIndexOf(".");
			String staticName = ctClass.getName().substring(lastIndexOf+1)+"."+name;
			staticFieldMap.put(staticName, field);
			staticValueAnno = new Annotation(STATIC_VALUE_PACKAGE, constPool);
			staticValueAnno.addMemberValue("name",new StringMemberValue(staticName, constPool));
		}
		Annotation refValueAnno = null;
		if (name.startsWith("&")) {
			String refName = upperFirstStr(name.replace("&", ""));
			while(staticFieldMap.get(refName)==null) {
				Thread.sleep(100);
				log.debug("get ref field "+refName+" is null, sleep 100ms");
			}
			Field refField = staticFieldMap.get(refName);
			int index = field.getIndex()!=null?field.getIndex():0;
			int offset = field.getOffset()!=null?field.getOffset():0;
			int lastIndexOf = refName.lastIndexOf(".");
			
			name = refName.substring(lastIndexOf+1);
			
			BeanUtils.copyProperties(refField, field);
			field.setName(name);
			field.setIndex(index);
			field.setLenght(0);
			field.setOffset(offset);
			field.setDependsOn(0);
			
			refValueAnno = new Annotation(REF_VALUE_PACKAGE, constPool);
			refValueAnno.addMemberValue("target",new StringMemberValue(refName, constPool));
		}
		
		
		CtField f = new CtField(getType(field.getType()), name, ctClass);
		f.setModifiers(Modifier.PRIVATE);
		ctClass.addField(f);
		String upperFieldStr = upperFirstStr(name);
		ctClass.addMethod(CtNewMethod.getter("get" + upperFieldStr, f));
		ctClass.addMethod(CtNewMethod.setter("set" + upperFieldStr, f));

		AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
		Annotation parser = new Annotation(PARSER_PACKAGE, constPool);
		parser.addMemberValue("index",
				new IntegerMemberValue(constPool, field.getIndex() != null ? field.getIndex() : 0));
		parser.addMemberValue("lenght",
				new IntegerMemberValue(constPool, field.getLenght() != null ? field.getLenght() : 0));
		parser.addMemberValue("offset",
				new IntegerMemberValue(constPool, field.getOffset() != null ? field.getOffset() : 0));
		parser.addMemberValue("dependsOn",
				new IntegerMemberValue(constPool, field.getDependsOn() != null ? field.getDependsOn() : 0));
		parser.addMemberValue("divide",
				new IntegerMemberValue(constPool, field.getDivide() != null ? field.getDivide() : 1));
		attr.addAnnotation(parser);


		if (field.getIsTransient() != null && field.getIsTransient()) {
			Annotation transientAnno = new Annotation(TRANSIENT_PACKAGE, constPool);
			attr.addAnnotation(transientAnno);
		}
		if (field.getIsLittleEnd() != null && field.getIsLittleEnd()) {
			Annotation LittleEndAnno = new Annotation(LITTLE_END_PACKAGE, constPool);
			attr.addAnnotation(LittleEndAnno);
		}
		if (field.getIdType() != null) {
			Annotation idAnno = new Annotation(ANNOTATION_ID_PACKAGE, constPool);
			EnumMemberValue enumValue = new EnumMemberValue(constPool);
			enumValue.setType(ID.TYPE.class.getName());
			enumValue.setValue(
					"AUTO".equals(field.getIdType().toUpperCase()) ? ID.TYPE.AUTO.name() : ID.TYPE.INPUT.name());
			idAnno.addMemberValue("value", enumValue);
			idAnno.addMemberValue("column",
					new StringMemberValue(CamelNameUtils.camel2underscore(field.getName()), constPool));
			attr.addAnnotation(idAnno);
		}
		if (field.getType() != null && field.getType().startsWith("list&")) {
			Annotation targetModelAnno = new Annotation(TARGET_MODEL_PACKAGE, constPool);
			int lastIndexOf = field.getType().lastIndexOf("&");
			targetModelAnno.addMemberValue("value",
					new StringMemberValue(MODEL_PACKAGE+upperFirstStr(field.getType().substring(lastIndexOf+1)), constPool));
			attr.addAnnotation(targetModelAnno);
		}
		if (refValueAnno != null) {
			attr.addAnnotation(refValueAnno);
		}
		if (staticValueAnno != null) {
			attr.addAnnotation(staticValueAnno);
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
		} else if (t != null && t.startsWith("list&")) {
			t = "java.util.List";
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
