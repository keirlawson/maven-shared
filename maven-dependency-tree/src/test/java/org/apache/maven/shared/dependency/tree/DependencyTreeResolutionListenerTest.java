package org.apache.maven.shared.dependency.tree;

/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Tests <code>DependencyTreeResolutionListener</code>.
 * 
 * @author Edwin Punzalan
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id$
 * @see DependencyTreeResolutionListener
 */
public class DependencyTreeResolutionListenerTest extends TestCase
{
    // constants --------------------------------------------------------------

    private static final Artifact[] EMPTY_ARTIFACTS = new Artifact[0];

    // fields -----------------------------------------------------------------

    private DependencyTreeResolutionListener listener;

    // TestCase methods -------------------------------------------------------

    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        listener = new DependencyTreeResolutionListener();
    }

    // tests ------------------------------------------------------------------

    public void testSimpleDependencyTree()
    {
        Artifact projectArtifact = createArtifact( "test-project", "project-artifact", "1.0" );
        listener.includeArtifact( projectArtifact );

        listener.startProcessChildren( projectArtifact );

        Artifact depArtifact01 = createArtifact( "test-dep", "dependency-one", "1.0" );
        listener.includeArtifact( depArtifact01 );

        Artifact depArtifact02 = createArtifact( "test-dep", "dependency-two", "1.0" );
        listener.includeArtifact( depArtifact02 );

        Artifact depArtifact03 = createArtifact( "test-dep", "dependency-three", "1.0" );
        listener.includeArtifact( depArtifact03 );

        listener.endProcessChildren( projectArtifact );

        Collection artifacts = listener.getNodes();
        assertTrue( "Check artifact lists match", compareNodeListToArtifacts( artifacts, new Artifact[] {
            depArtifact01, depArtifact02, depArtifact03, projectArtifact } ) );

        assertEquals( "Test dependency map key", projectArtifact, listener.getRootNode().getArtifact() );

        assertTrue( "Check artifact lists match", compareNodeListToArtifacts( listener.getRootNode().getChildren(),
                                                                              new Artifact[] { depArtifact01,
                                                                                  depArtifact02, depArtifact03 } ) );
    }

    public void testSimpleDepTreeWithTransitiveDeps()
    {
        Artifact projectArtifact = createArtifact( "test-project", "project-artifact", "1.0" );
        listener.includeArtifact( projectArtifact );

        listener.startProcessChildren( projectArtifact );

        Artifact depArtifact1 = createArtifact( "test-dep", "dependency-one", "1.0" );
        listener.includeArtifact( depArtifact1 );

        listener.startProcessChildren( depArtifact1 );

        Artifact depArtifact01 = createArtifact( "test-dep", "dependency-zero-one", "1.0" );
        listener.includeArtifact( depArtifact01 );

        Artifact depArtifact02 = createArtifact( "test-dep", "dependency-zero-two", "1.0" );
        listener.includeArtifact( depArtifact02 );

        listener.endProcessChildren( depArtifact1 );

        Artifact depArtifact2 = createArtifact( "test-dep", "dependency-two", "1.0" );
        listener.includeArtifact( depArtifact2 );

        Artifact depArtifact3 = createArtifact( "test-dep", "dependency-three", "1.0" );
        listener.includeArtifact( depArtifact3 );

        listener.endProcessChildren( projectArtifact );

        Collection artifacts = listener.getNodes();
        assertTrue( compareNodeListToArtifacts( artifacts, new Artifact[] { depArtifact1, depArtifact2, depArtifact3,
            depArtifact01, depArtifact02, projectArtifact } ) );

        assertEquals( "Check root", projectArtifact, listener.getRootNode().getArtifact() );
        assertTrue( compareNodeListToArtifacts( listener.getRootNode().getChildren(), new Artifact[] { depArtifact1,
            depArtifact2, depArtifact3 } ) );

        DependencyNode depNode1 = getChild( listener.getRootNode(), depArtifact1 );
        assertTrue( compareNodeListToArtifacts( depNode1.getChildren(), new Artifact[] { depArtifact01, depArtifact02 } ) );
    }

    public void testComplexDependencyTree()
    {
        Artifact projectArtifact = createArtifact( "test-project", "project-artifact", "1.0" );
        listener.includeArtifact( projectArtifact );

        listener.startProcessChildren( projectArtifact );

        Artifact depArtifact1 = createArtifact( "test-dep", "dependency-one", "1.0", Artifact.SCOPE_COMPILE );
        listener.includeArtifact( depArtifact1 );

        listener.startProcessChildren( depArtifact1 );

        Artifact depArtifact11 = createArtifact( "test-dep", "dependency-zero-one", "1.0" );
        listener.includeArtifact( depArtifact11 );

        Artifact depArtifact12 = createArtifact( "test-dep", "dependency-zero-two", "1.0" );
        listener.includeArtifact( depArtifact12 );

        listener.startProcessChildren( depArtifact12 );

        Artifact depArtifact121 = createArtifact( "test-dep", "dep-zero-two-1", "1.0" );
        listener.includeArtifact( depArtifact121 );

        listener.endProcessChildren( depArtifact12 );

        listener.endProcessChildren( depArtifact1 );

        Artifact depArtifact2 = createArtifact( "test-dep", "dependency-two", "1.0", Artifact.SCOPE_TEST );
        listener.includeArtifact( depArtifact2 );

        listener.startProcessChildren( depArtifact2 );

        Artifact depArtifact21 = createArtifact( "test-dep", "dep-zero-two-1", "1.0" );
        listener.includeArtifact( depArtifact21 );
        listener.omitForNearer( depArtifact121, depArtifact21 );

        listener.endProcessChildren( depArtifact2 );

        Artifact depArtifact3 = createArtifact( "test-dep", "dependency-three", "1.0", Artifact.SCOPE_COMPILE );
        listener.includeArtifact( depArtifact3 );

        listener.endProcessChildren( projectArtifact );

        Collection artifacts = listener.getNodes();
        assertTrue( compareNodeListToArtifacts( artifacts, new Artifact[] { depArtifact1, depArtifact2, depArtifact3,
            depArtifact11, depArtifact12, depArtifact21, projectArtifact } ) );

        assertEquals( projectArtifact, listener.getRootNode().getArtifact() );

        assertTrue( compareNodeListToArtifacts( listener.getRootNode().getChildren(), new Artifact[] { depArtifact1,
            depArtifact2, depArtifact3 } ) );

        DependencyNode node = getChild( listener.getRootNode(), depArtifact1 );
        assertTrue( compareNodeListToArtifacts( node.getChildren(), new Artifact[] { depArtifact11, depArtifact12 } ) );

        node = getChild( node, depArtifact12 );
        assertTrue( compareNodeListToArtifacts( node.getChildren(), EMPTY_ARTIFACTS ) );

        node = getChild( listener.getRootNode(), depArtifact2 );
        assertTrue( compareNodeListToArtifacts( node.getChildren(), new Artifact[] { depArtifact21 } ) );
    }

    // private methods --------------------------------------------------------

    private boolean compareNodeListToArtifacts( Collection nodes, Artifact[] artifacts )
    {
        List artifactsRemaining = new ArrayList( Arrays.asList( artifacts ) );

        for ( Iterator i = nodes.iterator(); i.hasNext(); )
        {
            DependencyNode node = (DependencyNode) i.next();

            if ( !artifactsRemaining.remove( node.getArtifact() ) )
            {
                return false;
            }
        }
        return artifactsRemaining.isEmpty();
    }

    private DependencyNode getChild( DependencyNode node, Artifact artifact )
    {
        DependencyNode result = null;
        for ( Iterator i = node.getChildren().iterator(); i.hasNext() && result == null; )
        {
            DependencyNode child = (DependencyNode) i.next();
            if ( child.getArtifact().equals( artifact ) )
            {
                result = child;
            }
        }
        return result;
    }

    private Artifact createArtifact( String groupId, String artifactId, String version )
    {
        return createArtifact( groupId, artifactId, version, null );
    }

    private Artifact createArtifact( String groupId, String artifactId, String version, String scope )
    {
        VersionRange versionRange = VersionRange.createFromVersion( version );

        return new DefaultArtifact( groupId, artifactId, versionRange, scope, "jar", null,
                                    new DefaultArtifactHandler(), false );
    }
}
