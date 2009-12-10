/**********************************************************************
 * Copyright (c) 2005-2009 ant4eclipse project team.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nils Hartmann, Daniel Kasmeroglu, Gerd Wuetherich
 **********************************************************************/
package org.ant4eclipse.jdt.ecj.internal.tools;

import org.ant4eclipse.core.Assert;
import org.ant4eclipse.core.ClassName;
import org.ant4eclipse.core.logging.A4ELogging;

import org.ant4eclipse.jdt.ecj.ClassFile;
import org.ant4eclipse.jdt.ecj.ClassFileLoader;
import org.ant4eclipse.jdt.ecj.SourceFile;

import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

/**
 * <p>
 * Adapter class to utilize class file loaders in the eclipse java compiler.
 * </p>
 * 
 * @author Gerd Wuetherich (gerd@gerd-wuetherich.de)
 * @author Nils Hartmann (nils@nilshartmann.net)
 */
public class NameEnvironmentImpl implements INameEnvironment {

  /** used to find binary type as requested by the compiler */
  private ClassFileLoader _classFileLoader;

  /**
   * <p>
   * Create a new instance of type {@link NameEnvironmentImpl}.
   * </p>
   * 
   * @param classFileLoader
   *          the class file loader to use.
   */
  public NameEnvironmentImpl(ClassFileLoader classFileLoader) {
    Assert.notNull(classFileLoader);

    this._classFileLoader = classFileLoader;
  }

  /**
   * @see org.eclipse.jdt.internal.compiler.env.INameEnvironment#cleanup()
   */
  public void cleanup() {
    // nothing to do here...
  }

  /**
   * {@inheritDoc}
   */
  public NameEnvironmentAnswer findType(char[][] compoundTypeName) {

    // convert char array to string(buffer)
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < compoundTypeName.length; i++) {
      buffer.append(new String(compoundTypeName[i]));
      if (i < compoundTypeName.length - 1) {
        buffer.append(".");
      }
    }

    // find class
    NameEnvironmentAnswer answer = findClass(buffer.toString());
    return answer;

  }

  /**
   * {@inheritDoc}
   */
  public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {

    StringBuffer result = new StringBuffer();
    if (packageName != null) {
      for (char[] element : packageName) {
        result.append(new String(element)).append(".");
      }
    }
    result.append(new String(typeName));

    return findClass(result.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * This method returns true if the given packageName is a valid package.
   * 
   * <p>
   * Note that this method returns true regardless of any OSGi visibility constraints.
   * <p>
   * This method also returns true for top level packages
   */
  public boolean isPackage(char[][] parentPackageName, char[] packageName) {

    String qualifiedPackageName = toJavaName(parentPackageName);
    if (qualifiedPackageName.length() > 0) {
      qualifiedPackageName += "." + new String(packageName);
    } else {
      qualifiedPackageName = new String(packageName);
    }

    boolean packageFound = this._classFileLoader.hasPackage(qualifiedPackageName);

    if (A4ELogging.isTraceingEnabled()) {
      if (packageFound) {
        A4ELogging.trace("Package '%s' found...", qualifiedPackageName);
      } else {
        A4ELogging.trace("Package '%s' not found...", qualifiedPackageName);
      }
    }

    return packageFound;
  }

  /**
   * @param className
   * @return
   */
  protected NameEnvironmentAnswer findClass(String className) {
    Assert.notNull(className);
    return findClass(ClassName.fromQualifiedClassName(className));
  }

  /**
   * <p>
   * Returns a {@link NameEnvironmentAnswer} for the class that is represented by the given {@link ClassName} instance.
   * </p>
   * 
   * @param className
   *          represents the class name
   * @return a {@link NameEnvironmentAnswer}
   */
  protected NameEnvironmentAnswer findClass(ClassName className) {

    // load class file from class file loader
    ClassFile classFile = this._classFileLoader.loadClass(className);

    // return new NameEnvironmentAnswer if classFile was found
    if (classFile != null) {

      if (A4ELogging.isTraceingEnabled()) {
        A4ELogging.trace("Loading class '%s' from '%s'.", className.getQualifiedClassName(), classFile
            .getLibraryLocation());
      }

      return new NameEnvironmentAnswer(classFile.getBinaryType(), (classFile.hasAccessRestriction() ? classFile
          .getAccessRestriction() : null));
    }

    // load source file from class file loader
    // TODO: AccessRestictions for source files!!
    SourceFile sourceFile = this._classFileLoader.loadSource(className);

    // load source file from class file loader
    if (sourceFile != null) {

      if (A4ELogging.isTraceingEnabled()) {
        A4ELogging.trace("Loading class '%s' as source from '%s'.", className.getQualifiedClassName(), sourceFile
            .getSourceFile().getAbsolutePath());
      }

      // TODO: AccessRestictions for source files!!
      return new NameEnvironmentAnswer(new CompilationUnitImpl(sourceFile), null);
    }

    // else return null
    if (A4ELogging.isTraceingEnabled()) {
      A4ELogging.trace("Could not load class '%s'.", className.getQualifiedClassName());
    }
    return null;
  }

  /**
   * <p>
   * Converts the given char array to a java name (e.g. "net.sf.ant4eclipse").
   * </p>
   * 
   * @param array
   *          the array to convert.
   * @return the
   */
  private static String toJavaName(char[][] array) {
    StringBuffer result = new StringBuffer();

    if (array != null) {
      for (int i = 0; i < array.length; i++) {
        result.append(new String(array[i]));
        if (i < array.length - 1) {
          result.append(".");
        }
      }
    }
    return result.toString();
  }

  // /**
  // * <p>
  // * ONLY USED FOR DEBUGGING PURPOSE.
  // * </p>
  // *
  // * @param array
  // * @return
  // */
  // private static String asString(char[][] array) {
  // // define result
  // StringBuffer result = new StringBuffer();
  //
  // // compute result
  // if (array != null) {
  // for (int i = 0; i < array.length; i++) {
  // result.append("{").append(new String(array[i])).append("}");
  // if (i < array.length - 1) {
  // result.append(",");
  // }
  // }
  // } else {
  // result.append("(null)");
  // }
  //
  // // return result
  // return result.toString();
  // }
}
