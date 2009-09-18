package org.ant4eclipse.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

import junit.framework.TestCase;

public class UtilitiesTest extends TestCase {

  private File _expansionDirectory;

  @Before
  public void init() {

    String tempDirString = System.getProperty("java.io.tmpdir");
    this._expansionDirectory = new File(tempDirString);

    Utilities.delete(new File(this._expansionDirectory, "test.jar"));
    Utilities.delete(new File(this._expansionDirectory, "test.txt"));
    Utilities.delete(new File(this._expansionDirectory, "test2.jar"));
    Utilities.delete(new File(this._expansionDirectory, "META-INF/Manifest.mf"));
  }

  @After
  public void dispose() {
    Utilities.delete(new File(this._expansionDirectory, "test.jar"));
    Utilities.delete(new File(this._expansionDirectory, "test.txt"));
    Utilities.delete(new File(this._expansionDirectory, "test2.jar"));
    Utilities.delete(new File(this._expansionDirectory, "META-INF/Manifest.mf"));
  }

  @Test
  public void test() {

    URL url = getClass().getClassLoader().getResource("org/ant4eclipse/core/util/test-jar.jar");
    File jarFile = new File(url.getFile());

    assertTrue(jarFile.exists() && jarFile.isFile());

    assertTrue(this._expansionDirectory.exists() && this._expansionDirectory.isDirectory());

    try {
      Utilities.expandJarFile(new JarFile(jarFile), this._expansionDirectory);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertTrue(new File(this._expansionDirectory, "test.jar").exists());
    assertTrue(new File(this._expansionDirectory, "test.txt").exists());
    assertTrue(new File(this._expansionDirectory, "test2.jar").exists());
    assertTrue(new File(this._expansionDirectory, "META-INF/MANIFEST.MF").exists());
  }

  public void testCalcRelative() {

    String relative = Utilities.calcRelative(new File("/schnerd"), new File("/temp/rep/schrepp/depp"));
    System.out.println(relative);
  }

  public void test_newInstance() {
    Dummy dummy = Utilities.newInstance(Dummy.class.getName());
    assertNotNull(dummy);
  }

  public static class Dummy {
    public Dummy() {
      // needed
    }
  }
}
