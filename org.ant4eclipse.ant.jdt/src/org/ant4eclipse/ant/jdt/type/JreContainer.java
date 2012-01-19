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
package org.ant4eclipse.ant.jdt.type;

import org.ant4eclipse.ant.core.AbstractAnt4EclipseDataType;
import org.ant4eclipse.lib.core.A4ECore;
import org.ant4eclipse.lib.core.logging.A4ELogging;
import org.ant4eclipse.lib.core.util.Utilities;
import org.ant4eclipse.lib.jdt.model.ContainerTypes;
import org.ant4eclipse.lib.jdt.model.jre.JavaRuntime;
import org.ant4eclipse.lib.jdt.model.jre.JavaRuntimeRegistry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A datatype used as a container for classpathes.
 * 
 * @author Daniel Kasmeroglu (daniel.kasmeroglu@kasisoft.net)
 */
public class JreContainer extends AbstractAnt4EclipseDataType {

  private String _defaultJre;

  /**
   * Simply initialises this new type.
   * 
   * @param project
   *          The project this type applies to.
   */
  public JreContainer( Project project ) {
    super( project );
    _defaultJre = null;
  }

  /**
   * Creates the entry for a java runtime environment.
   * 
   * @return An entry for a java runtime environment.
   */
  public Runtime createJre() {
    return new Runtime();
  }

  public void setDefault( String defaultJre ) {
    _defaultJre = defaultJre;
  }

  /**
   * Adds the supplied java runtime environment to this type after it has been configured..
   * 
   * @param runtime
   *          The java runtime environment configuration that shall be added.
   */
  public void addConfiguredJre( Runtime runtime ) {
    File location = runtime.getLocation();
    if( location == null ) {
      throw new BuildException( "Missing parameter 'location' on jre!" );
    }

    if( !Utilities.hasText( runtime.getId() ) ) {
      throw new BuildException( "Missing parameter 'id' on jre!" );
    }

    boolean isDefault = runtime.getId().equals( _defaultJre );

    JavaRuntimeRegistry javaRuntimeRegistry = A4ECore.instance().getRequiredService( JavaRuntimeRegistry.class );

    // If specified: add files for jre (otherwise required JRE jars are determined automatically)
    List<File> jreFiles = getSelectedJreFiles( runtime );

    JavaRuntime javaRuntime = javaRuntimeRegistry
        .registerJavaRuntime( runtime.getId(), runtime.getLocation(), jreFiles );

    if( javaRuntime == null ) {
      throw new RuntimeException( String.format( "Missing java runtime : %s", runtime.getId() ) );
    }
    
    if( isDefault ) {
      javaRuntimeRegistry.setDefaultJavaRuntime( runtime.getId() );
    }

    Path path = new Path( getProject() );
    List<File> libraries = javaRuntime.getLibraries();
    for( File librarie : libraries ) {
      path.createPathElement().setLocation( librarie );
    }

    getProject().addReference( String.format( "%s%s", ContainerTypes.VMTYPE_PREFIX, runtime.getId() ), path );

    // register default JRE as JRE_CONTAINER too
    if( isDefault ) {
      A4ELogging.debug( "Registered default JRE with id '%s'", ContainerTypes.JRE_CONTAINER );
      getProject().addReference( ContainerTypes.JRE_CONTAINER, path );
    }
  }

  /**
   * Returns the files that are selected by {@link FileSet FileSets} for the specified runtime.
   * <p>
   * Returns <tt>null</tt> if there are files specified explicitly
   * 
   * @param runtime
   * @return
   */
  private List<File> getSelectedJreFiles( Runtime runtime ) {
    if( !runtime.hasFileSets() ) {
      return null;
    }
    List<File> files = new ArrayList<File>();
    List<FileSet> fileSets = runtime.getFileSets();
    for( FileSet fileSet : fileSets ) {
      DirectoryScanner directoryScanner = fileSet.getDirectoryScanner( getProject() );
      File dir = fileSet.getDir();
      String[] includedFiles = directoryScanner.getIncludedFiles();
      for( String includedFile : includedFiles ) {
        files.add( new File( dir, includedFile ) );
      }
    }
    return files;
  }

  public static class Runtime {

    private String        _id;

    private File          _location;

    private List<FileSet> _fileSets;

    public String getId() {
      return _id;
    }

    public void setId( String newid ) {
      _id = newid;
    }

    public File getLocation() {
      return _location;
    }

    public void setLocation( File newlocation ) {
      _location = newlocation;
    }

    public void addFileSet( FileSet newfileset ) {
      if( _fileSets == null ) {
        _fileSets = new ArrayList<FileSet>();
      }
      _fileSets.add( newfileset );
    }

    /**
     * Might return null if no filesets have been specified by the user
     * 
     * @return
     */
    public List<FileSet> getFileSets() {
      return _fileSets;
    }

    public boolean hasFileSets() {
      return _fileSets != null;
    }
  }
} /* ENDCLASS */
