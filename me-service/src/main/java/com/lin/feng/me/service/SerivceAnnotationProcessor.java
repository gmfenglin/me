package com.lin.feng.me.service;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes("com.lin.feng.me.service.MeService")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SerivceAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		HashMap<String, Set<String>> records = new HashMap<>();
		roundEnv.getElementsAnnotatedWith(MeService.class).stream().filter((element) -> {
			TypeElement currentClass = (TypeElement) element;
			return currentClass.getKind() == ElementKind.CLASS && currentClass.getModifiers().contains(Modifier.PUBLIC)
					&& hasCorrectConstructor(currentClass);
		}).forEach((element) -> {
			try {
				TypeElement currentClass = (TypeElement) element;
				currentClass.getAnnotationMirrors().stream().filter((annotation) -> {
					return ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName()
							.contentEquals(MeService.class.getName());
				}).forEach((annotation) -> {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
							annotation.getElementValues().toString());
					String key = null;
					for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation
							.getElementValues().entrySet()) {
						if (((ExecutableElement) entry.getKey()).getSimpleName().contentEquals("value")) {
							key = entry.getValue().getValue().toString();
							if (key != null && !records.containsKey(key)) {
								records.put(key, new HashSet<String>());
							}
							break;
						}
					}

					String name = currentClass.getSimpleName().toString();
					for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation
							.getElementValues().entrySet()) {
						if (((ExecutableElement) entry.getKey()).getSimpleName().contentEquals("name")) {
							String tmpName = entry.getValue().getValue().toString();
							if (tmpName != null && !tmpName.equals("")) {
								name = tmpName;
							}
							break;
						}
					}
					String first = name.substring(0, 1).toLowerCase();
					name = first + name.substring(1, name.length());
					records.get(key).add(name + "=" + currentClass.getQualifiedName());
				});

			} catch (Exception e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Exception process:" + e.getMessage());
			}

		});
		records.forEach((key, value) -> {
			try {
				FileObject output = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
						"META-INF/me/services/" + key, new Element[0]);
				Writer writer = output.openWriter();
				try {
					value.forEach((serviceInfo) -> {
						try {
							processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, serviceInfo);
							writer.write(serviceInfo + "\n");
						} catch (Exception e) {
							// TODO: handle exception
						}

					});
				} finally {
					try {
						writer.close();
					} catch (IOException e) {
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		});

		return true;
	}

	private boolean hasCorrectConstructor(TypeElement currentClass) {
		List<ExecutableElement> constructors = ElementFilter.constructorsIn(currentClass.getEnclosedElements());
		for (ExecutableElement constructor : constructors) {
			if ((constructor.getModifiers().contains(Modifier.PUBLIC)) && (constructor.getParameters().isEmpty())) {
				return true;
			}
		}
		return false;
	}

}
