/**
 * Copyright (c) 2013, Andre Steingress
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1.) Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 2.) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.) Neither the name of Andre Steingress nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gcontracts.doc;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.codehaus.groovy.ant.Groovydoc;
import org.codehaus.groovy.ant.LoggingHelper;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.*;
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * @author ast
 */
public class ContractGroovyDoc extends Groovydoc {

    private final LoggingHelper log = new LoggingHelper(this);

    private Path sourcePath;
    private File destDir;
    private List<String> packageNames;
    private List<String> excludePackageNames;

    private List<String> docTemplates = new ArrayList<String>(Arrays.asList(GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES));
    private List<String> packageTemplates = new ArrayList<String>(Arrays.asList(GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES));
    private List<String> classTemplates = new ArrayList<String>(Arrays.asList("org/gcontracts/doc/templates/classDocName.html"));

    private String windowTitle = "Groovy Documentation";
    private String docTitle = "Groovy Documentation";
    private String footer = "Groovy Documentation";
    private String header = "Groovy Documentation";
    private Boolean privateScope;
    private Boolean protectedScope;
    private Boolean packageScope;
    private Boolean publicScope;
    private Boolean author;
    private Boolean processScripts;
    private Boolean includeMainForScripts;
    private boolean useDefaultExcludes;
    private boolean includeNoSourcePackages;
    private List<DirSet> packageSets;
    private List<String> sourceFilesToDoc;
    private List<LinkArgument> links = new ArrayList<LinkArgument>();
    private File overviewFile;
    private File styleSheetFile;
    // dev note: update javadoc comment for #setExtensions(String) if updating below
    private String extensions = ".java:.groovy:.gv:.gvy:.gsh";

    public ContractGroovyDoc() {
        packageNames = new ArrayList<String>();
        excludePackageNames = new ArrayList<String>();
        packageSets = new ArrayList<DirSet>();
        sourceFilesToDoc = new ArrayList<String>();
        privateScope = false;
        protectedScope = false;
        publicScope = false;
        packageScope = false;
        useDefaultExcludes = true;
        includeNoSourcePackages = false;
        author = true;
        processScripts = true;
        includeMainForScripts = true;
    }

    public void setDocTemplates(String docTemplates) {
        scanTemplateFileset(this.docTemplates, docTemplates);
    }

    public void setPackageTemplates(String packageTemplates) {
        scanTemplateFileset(this.packageTemplates, packageTemplates);
    }

    public void setClassTemplates(String classTemplates) {
        scanTemplateFileset(this.classTemplates, classTemplates);
    }

    private void scanTemplateFileset(List<String> templates, String fileset)  {
        if (templates == null) throw new IllegalArgumentException("Parameter 'templates' must not be null!");
        if (fileset != null && fileset.length() > 0)  {
            String[] tokens = fileset.split(";");
            if (tokens.length == 0) return;

            templates.clear();

            for (String token : tokens)  {
                templates.add(token);
            }
        }
    }

    /**
     * Specify where to find source file
     *
     * @param src a Path instance containing the various source directories.
     */
    public void setSourcepath(Path src) {
        if (sourcePath == null) {
            sourcePath = src;
        } else {
            sourcePath.append(src);
        }
    }

    /**
     * Set the directory where the Groovydoc output will be generated.
     *
     * @param dir the destination directory.
     */
    public void setDestdir(File dir) {
        destDir = dir;
        // todo: maybe tell groovydoc to use file output
    }

    /**
     * If set to false, author will not be displayed.
     * Currently not used.
     *
     * @param author new value
     */
    public void setAuthor(boolean author) {
        this.author = author;
    }

    /**
     * If set to false, Scripts will not be processed.
     * Defaults to true.
     *
     * @param processScripts new value
     */
    public void setProcessScripts(boolean processScripts) {
        this.processScripts = processScripts;
    }

    /**
     * If set to false, 'public static void main' method will not be displayed.
     * Defaults to true. Ignored when not processing Scripts.
     *
     * @param includeMainForScripts new value
     */
    public void setIncludeMainForScripts(boolean includeMainForScripts) {
        this.includeMainForScripts = includeMainForScripts;
    }

    /**
     * A colon-separated list of filename extensions to look for when searching for files to process in a given directory.
     * Default value: <code>.java:.groovy:.gv:.gvy:.gsh</code>
     *
     * @param extensions new value
     */
    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    /**
     * Set the package names to be processed.
     *
     * @param packages a comma separated list of packages specs
     *                 (may be wildcarded).
     */
    public void setPackagenames(String packages) {
        StringTokenizer tok = new StringTokenizer(packages, ",");
        while (tok.hasMoreTokens()) {
            String packageName = tok.nextToken();
            packageNames.add(packageName);
        }
    }

    public void setUse(boolean b) {
        //ignore as 'use external file' irrelevant with groovydoc :-)
    }

    /**
     * Set the title to be placed in the HTML &lt;title&gt; tag of the
     * generated documentation.
     *
     * @param title the window title to use.
     */
    public void setWindowtitle(String title) {
        windowTitle = title;
    }

    /**
     * Set the title for the overview page.
     *
     * @param htmlTitle the html to use for the title.
     */
    public void setDoctitle(String htmlTitle) {
        docTitle = htmlTitle;
    }

    /**
     * Specify the file containing the overview to be included in the generated documentation.
     *
     * @param file the overview file
     */
    public void setOverview(File file) {
        overviewFile = file;
    }

    /**
     * Indicates the access mode or scope of interest: one of public, protected, package, or private.
     * Package scoped access is ignored for fields of Groovy classes where they correspond to properties.
     *
     * @param access one of public, protected, package, or private
     */
    public void setAccess(String access) {
        if ("public".equals(access)) publicScope = true;
        else if ("protected".equals(access)) protectedScope = true;
        else if ("package".equals(access)) packageScope = true;
        else if ("private".equals(access)) privateScope = true;
    }

    /**
     * Indicate whether all classes and
     * members are to be included in the scope processed.
     *
     * @param b true if scope is to be private level.
     */
    public void setPrivate(boolean b) {
        privateScope = b;
    }

    /**
     * Indicate whether only public classes and members are to be included in the scope processed.
     *
     * @param b true if scope only includes public level classes and members
     */
    public void setPublic(boolean b) {
        publicScope = b;
    }

    /**
     * Indicate whether only protected and public classes and members are to be included in the scope processed.
     *
     * @param b true if scope includes protected level classes and members
     */
    public void setProtected(boolean b) {
        protectedScope = b;
    }

    /**
     * Indicate whether only package, protected and public classes and members are to be included in the scope processed.
     * Package scoped access is ignored for fields of Groovy classes where they correspond to properties.
     *
     * @param b true if scope includes package level classes and members
     */
    public void setPackage(boolean b) {
        packageScope = b;
    }

    /**
     * Set the footer to place at the bottom of each generated html page.
     *
     * @param footer the footer value
     */
    public void setFooter(String footer) {
        this.footer = footer;
    }

    /**
     * Specifies the header text to be placed at the top of each output file.
     * The header will be placed to the right of the upper navigation bar.
     * It may contain HTML tags and white space, though if it does, it must
     * be enclosed in quotes. Any internal quotation marks within the header
     * may have to be escaped.
     *
     * @param header the header value
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Specifies a stylesheet file to use. If not specified,
     * a default one will be generated for you.
     *
     * @param styleSheetFile the css stylesheet file to use
     */
    public void setStyleSheetFile(File styleSheetFile) {
        this.styleSheetFile = styleSheetFile;
    }

    /**
     * Add the directories matched by the nested dirsets to the resulting
     * packages list and the base directories of the dirsets to the Path.
     * It also handles the packages and excludepackages attributes and
     * elements.
     *
     * @param resultantPackages a list to which we add the packages found
     * @param sourcePath a path to which we add each basedir found
     * @since 1.5
     */
    private void parsePackages(List<String> resultantPackages, Path sourcePath) {
        List<String> addedPackages = new ArrayList<String>();
        List<DirSet> dirSets = new ArrayList<DirSet>(packageSets);

        // for each sourcePath entry, add a directoryset with includes
        // taken from packagenames attribute and nested package
        // elements and excludes taken from excludepackages attribute
        // and nested excludepackage elements
        if (this.sourcePath != null) {
            PatternSet ps = new PatternSet();
            if (packageNames.size() > 0) {
                for (String pn : packageNames) {
                    String pkg = pn.replace('.', '/');
                    if (pkg.endsWith("*")) {
                        pkg += "*";
                    }
                    ps.createInclude().setName(pkg);
                }
            } else {
                ps.createInclude().setName("**");
            }

            for (String epn : excludePackageNames) {
                String pkg = epn.replace('.', '/');
                if (pkg.endsWith("*")) {
                    pkg += "*";
                }
                ps.createExclude().setName(pkg);
            }

            String[] pathElements = this.sourcePath.list();
            for (String pathElement : pathElements) {
                File dir = new File(pathElement);
                if (dir.isDirectory()) {
                    DirSet ds = new DirSet();
                    ds.setDefaultexcludes(useDefaultExcludes);
                    ds.setDir(dir);
                    ds.createPatternSet().addConfiguredPatternset(ps);
                    dirSets.add(ds);
                } else {
                    log.warn("Skipping " + pathElement + " since it is no directory.");
                }
            }
        }

        for (DirSet ds : dirSets) {
            File baseDir = ds.getDir(getProject());
            log.debug("scanning " + baseDir + " for packages.");
            DirectoryScanner dsc = ds.getDirectoryScanner(getProject());
            String[] dirs = dsc.getIncludedDirectories();
            boolean containsPackages = false;
            for (String dir : dirs) {
                // are there any groovy or java files in this directory?
                File pd = new File(baseDir, dir);
                String[] files = pd.list(new FilenameFilter() {
                    public boolean accept(File dir1, String name) {
                        if (!includeNoSourcePackages
                                && name.equals("package.html")) return true;
                        final StringTokenizer tokenizer = new StringTokenizer(extensions, ":");
                        while (tokenizer.hasMoreTokens()) {
                            String ext = tokenizer.nextToken();
                            if (name.endsWith(ext)) return true;
                        }
                        return false;
                    }
                });

                for (String filename : Arrays.asList(files)) {
                    sourceFilesToDoc.add(dir + File.separator + filename);
                }

                if (files.length > 0) {
                    if ("".equals(dir)) {
                        log.warn(baseDir
                                + " contains source files in the default package,"
                                + " you must specify them as source files not packages.");
                    } else {
                        containsPackages = true;
                        String pn = dir.replace(File.separatorChar, '.');
                        if (!addedPackages.contains(pn)) {
                            addedPackages.add(pn);
                            resultantPackages.add(pn);
                        }
                    }
                }
            }
            if (containsPackages) {
                // We don't need to care for duplicates here,
                // Path.list does it for us.
                sourcePath.createPathElement().setLocation(baseDir);
            } else {
                log.verbose(baseDir + " doesn't contain any packages, dropping it.");
            }
        }
    }

    public void execute() throws BuildException {
        List<String> packagesToDoc = new ArrayList<String>();
        Path sourceDirs = new Path(getProject());
        Properties properties = new Properties();
        properties.setProperty("windowTitle", windowTitle);
        properties.setProperty("docTitle", docTitle);
        properties.setProperty("footer", footer);
        properties.setProperty("header", header);
        checkScopeProperties(properties);
        properties.setProperty("publicScope", publicScope.toString());
        properties.setProperty("protectedScope", protectedScope.toString());
        properties.setProperty("packageScope", packageScope.toString());
        properties.setProperty("privateScope", privateScope.toString());
        properties.setProperty("author", author.toString());
        properties.setProperty("processScripts", processScripts.toString());
        properties.setProperty("includeMainForScripts", includeMainForScripts.toString());
        properties.setProperty("overviewFile", overviewFile != null ? overviewFile.getAbsolutePath() : "");

        if (sourcePath != null) {
            sourceDirs.addExisting(sourcePath);
        }
        parsePackages(packagesToDoc, sourceDirs);

        if (classTemplates.size() == 0)
            throw new BuildException("Method getClassTemplates() needs to return at least a single classTemplate String!");

        GroovyDocTool htmlTool = new GroovyDocTool(
                createResourceManager(),
                sourcePath.list(),
                getDocTemplates(),
                getPackageTemplates(),
                getClassTemplates(),
                links,
                properties
        );

        try {
            htmlTool.add(sourceFilesToDoc);
            FileOutputTool output = new FileOutputTool();
            htmlTool.renderToOutput(output, destDir.getCanonicalPath()); // TODO push destDir through APIs?
        } catch (Exception e) {
            e.printStackTrace();
        }
        // try to override the default stylesheet with custom specified one if needed
        if (styleSheetFile != null) {
            try {
                String css = DefaultGroovyMethods.getText(styleSheetFile);
                File outfile = new File(destDir, "stylesheet.css");
                DefaultGroovyMethods.setText(outfile, css);
            } catch (IOException e) {
                System.out.println("Warning: Unable to copy specified stylesheet '" + styleSheetFile.getAbsolutePath() +
                        "'. Using default stylesheet instead. Due to: " + e.getMessage());
            }
        }
    }

    private void checkScopeProperties(Properties properties) {
        // make protected the default scope and check for invalid duplication
        int scopeCount = 0;
        if (packageScope) scopeCount++;
        if (privateScope) scopeCount++;
        if (protectedScope) scopeCount++;
        if (publicScope) scopeCount++;
        if (scopeCount == 0) {
            protectedScope = true;
        } else if (scopeCount > 1) {
            throw new BuildException("More than one of public, private, package, or protected scopes specified.");
        }
    }

    /**
     * Create link to Javadoc/GroovyDoc output at the given URL.
     *
     * @return link argument to configure
     */
    public LinkArgument createLink() {
        LinkArgument result = new LinkArgument();
        links.add(result);
        return result;
    }

    /**
     * Create a {@link ResourceManager} instance which is used to resolve GroovyDoc templates.
     *
     * @return an instance of {@link ResourceManager} used to resolve GroovyDoc templates
     */
    protected ResourceManager createResourceManager()  {
        return new ClasspathResourceManager();
    }

    /**
     * A list of top-level document templates, see {@link GroovyDocTemplateInfo#DEFAULT_DOC_TEMPLATES} as an example.
     *
     * @return a String array of top-level document templates
     */
    public String[] getDocTemplates() {
        return docTemplates.toArray(new String[docTemplates.size()]);
    }

    /**
     * A list of package-level document templates, see {@link GroovyDocTemplateInfo#DEFAULT_PACKAGE_TEMPLATES} as an example.
     *
     * @return a String array of package-level document templates
     */
    public String[] getPackageTemplates() {
        return packageTemplates.toArray(new String[packageTemplates.size()]);
    }

    /**
     * A list of class-level document templates, see {@link GroovyDocTemplateInfo#DEFAULT_DOC_TEMPLATES} as an example.
     *
     * @return a String array of class-level document templates
     */
    public String[] getClassTemplates()  {
        return classTemplates.toArray(new String[classTemplates.size()]);
    }
}
