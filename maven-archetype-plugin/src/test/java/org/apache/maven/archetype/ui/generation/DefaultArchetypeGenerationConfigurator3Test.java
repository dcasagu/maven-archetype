package org.apache.maven.archetype.ui.generation;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Properties;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.DefaultInputHandler;
import org.codehaus.plexus.components.interactivity.DefaultPrompter;
import org.codehaus.plexus.components.interactivity.InputHandler;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.easymock.MockControl;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the ability to use variables in default fields in batch mode
 */
public class DefaultArchetypeGenerationConfigurator3Test
    extends PlexusTestCase
{
    private DefaultArchetypeGenerationConfigurator configurator;
    private ArchetypeGenerationQueryer archetypeGenerationQueryer;
    private Prompter keyboardMock;
    private MockControl keyboardControl;

    public void setUp()
        throws Exception
    {
        super.setUp();

        configurator = (DefaultArchetypeGenerationConfigurator) lookup( ArchetypeGenerationConfigurator.ROLE );
        archetypeGenerationQueryer = (DefaultArchetypeGenerationQueryer) lookup ( ArchetypeGenerationQueryer.class );

        ProjectBuildingRequest buildingRequest = null;
        
        MockControl control = MockControl.createControl( ArchetypeArtifactManager.class );
        control.setDefaultMatcher( MockControl.ALWAYS_MATCHER );

        ArchetypeArtifactManager manager = (ArchetypeArtifactManager) control.getMock();
        manager.exists( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null, buildingRequest );
        control.setReturnValue( true );
        manager.isFileSetArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null, buildingRequest );
        control.setReturnValue( true );
        manager.isOldArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null, buildingRequest );
        control.setReturnValue( false );
        manager.getFileSetArchetypeDescriptor( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null,
                                               null, null, buildingRequest );
        ArchetypeDescriptor descriptor = new ArchetypeDescriptor();
        RequiredProperty groupId = new RequiredProperty();
        groupId.setKey( "groupId" );
        groupId.setDefaultValue( "com.example.${projectName}" );
        RequiredProperty artifactId = new RequiredProperty();
        artifactId.setKey( "artifactId" );
        artifactId.setDefaultValue( "${projectName}-${domainName}" );
        RequiredProperty thePackage = new RequiredProperty();
        thePackage.setKey( "package" );
        thePackage.setDefaultValue( "com.example.${projectName}.${domainName}" );
        RequiredProperty projectName = new RequiredProperty();
        projectName.setKey( "projectName" );
        projectName.setDefaultValue( null );
        RequiredProperty domainName = new RequiredProperty();
        domainName.setKey( "domainName" );
        domainName.setDefaultValue( null );
        RequiredProperty snakeCaseProperty = new RequiredProperty();
        snakeCaseProperty.setKey( "snakeCaseProperty" );
        snakeCaseProperty.setDefaultValue( "${projectName.toUpperCase()}_${domainName.toUpperCase()}" );
        descriptor.addRequiredProperty( projectName );
        descriptor.addRequiredProperty( groupId );
        descriptor.addRequiredProperty( domainName );
        descriptor.addRequiredProperty( artifactId );
        descriptor.addRequiredProperty( thePackage );
        descriptor.addRequiredProperty(snakeCaseProperty);
        control.setReturnValue( descriptor );
        control.replay();
        configurator.setArchetypeArtifactManager( manager );   
        
        keyboardControl = MockControl.createControl( Prompter.class );   
        keyboardControl.setDefaultMatcher( MockControl.EQUALS_MATCHER );
        keyboardMock = (Prompter) keyboardControl.getMock();
        
        Field archetypeGenerationQueryerField = DefaultArchetypeGenerationConfigurator.class.getDeclaredField("archetypeGenerationQueryer");
        archetypeGenerationQueryerField.setAccessible(true);
        archetypeGenerationQueryerField.set(configurator, archetypeGenerationQueryer);
        
        Field prompterField = DefaultArchetypeGenerationQueryer.class.getDeclaredField("prompter");
        prompterField.setAccessible(true);
      //  prompterField.set(archetypeGenerationQueryer, keyboardMock);
        
    }

    public void testJIRA_562_PropertiesReferringEachOtherIndirectly() throws Exception
    {
                
        // Record Keyboard Inputs
        keyboardMock.prompt("Define value for property 'projectName'"); // projectName property without default value
        keyboardControl.setReturnValue("myprojectname");
        
        keyboardMock.prompt("Define value for property 'groupId'", "com.example.myprojectname"); // groupId property (confirm default proposed value)
        keyboardControl.setReturnValue("com.example.myprojectname");
        
        keyboardMock.prompt("Define value for property 'domainName'"); // domainName property without default value
        keyboardControl.setReturnValue("mydomainname");
        
        keyboardMock.prompt("Define value for property 'artifactId'", "myprojectname-mydomainname"); // artifactId property (confirm default proposed value)
        keyboardControl.setReturnValue("myprojectname-mydomainname");
        
        keyboardMock.prompt("Define value for property 'version'", "1.0-SNAPSHOT"); // version property (confirm default proposed value)
        keyboardControl.setReturnValue("1.0-SNAPSHOT");
        
        keyboardMock.prompt("Define value for property 'package'", "com.example.myprojectname.mydomainname"); // package property (confirm default proposed value)
        keyboardControl.setReturnValue("com.example.myprojectname.mydomainname");
        
        keyboardMock.prompt("Define value for property 'snakeCaseProperty'", "MYPROJECTNAME_MYDOMAINNAME");  // snake case property (confirm default proposed value)
        keyboardControl.setReturnValue("MYPROJECTNAME_MYDOMAINNAME");
        
        keyboardMock.prompt("Confirm properties configuration:\n"
                + "projectName: myprojectname\n"
                + "groupId: com.example.myprojectname\n"
                + "domainName: mydomainname\n"
                + "artifactId: myprojectname-mydomainname\n"
                + "version: 1.0-SNAPSHOT\n"
                + "package: com.example.myprojectname.mydomainname\n"
                + "snakeCaseProperty: MYPROJECTNAME_MYDOMAINNAME\n", "Y");  // Accept config (Default Y)
        keyboardControl.setReturnValue("Y");
        keyboardControl.replay();
        
        // Reproduce maven archetype generation (interactive mode)
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();
                
        // Simulate interactive mode to force param ordering
        configurator.configureArchetype( request, Boolean.TRUE, properties );
                
        keyboardControl.verify();
        
        assertEquals( "com.example.myprojectname", request.getGroupId() );
        assertEquals( "myprojectname-mydomainname", request.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", request.getVersion() );
        assertEquals( "com.example.myprojectname.mydomainname", request.getPackage() );
        assertEquals( "MYPROJECTNAME_MYDOMAINNAME", request.getProperties().get( "snakeCaseProperty" ) );
        
        
    }
          
}