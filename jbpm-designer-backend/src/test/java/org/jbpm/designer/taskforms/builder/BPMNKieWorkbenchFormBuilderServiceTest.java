package org.jbpm.designer.taskforms.builder;

import org.apache.commons.io.IOUtils;
import org.eclipse.bpmn2.Definitions;
import org.jbpm.designer.bpmn2.impl.Bpmn2UnmarshallingTest;
import org.jbpm.designer.bpmn2.utils.Bpmn2Loader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.forms.commons.layout.impl.StaticFormLayoutTemplateGenerator;
import org.kie.workbench.common.forms.fields.test.TestFieldManager;
import org.kie.workbench.common.forms.jbpm.model.authoring.process.BusinessProcessFormModel;
import org.kie.workbench.common.forms.jbpm.model.authoring.task.TaskFormModel;
import org.kie.workbench.common.forms.jbpm.server.service.impl.BPMNFormModelGeneratorImpl;
import org.kie.workbench.common.forms.jbpm.server.service.impl.BusinessProcessFormModelHandler;
import org.kie.workbench.common.forms.jbpm.server.service.impl.TaskFormModelHandler;
import org.kie.workbench.common.forms.model.FormDefinition;
import org.kie.workbench.common.forms.serialization.FormDefinitionSerializer;
import org.kie.workbench.common.forms.serialization.impl.FieldSerializer;
import org.kie.workbench.common.forms.serialization.impl.FormDefinitionSerializerImpl;
import org.kie.workbench.common.forms.serialization.impl.FormModelSerializer;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.vfs.Path;
import org.uberfire.io.IOService;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class BPMNKieWorkbenchFormBuilderServiceTest {

    @Mock
    private IOService ioService;

    @Mock
    private Path formPath;

    private Bpmn2Loader loader = new Bpmn2Loader( Bpmn2UnmarshallingTest.class );

    private Definitions definitions;

    private BPMNKieWorkbenchFormBuilderService builderService;
    private FormDefinitionSerializer formSerializer;

    @Before
    public void initTest() throws Exception {
        when( formPath.toURI() ).thenReturn( "file://fakepath.frm" );
        when( ioService.exists( any() ) ).thenReturn( false );

        definitions = loader.loadProcessFromJson( "userTask.json" );

        formSerializer = new FormDefinitionSerializerImpl( new FieldSerializer(),
                                                           new FormModelSerializer() );

        builderService = new BPMNKieWorkbenchFormBuilderService( ioService,
                                                                 clazz -> {
                                                                     if ( clazz.equals( BusinessProcessFormModel.class ) ) {
                                                                         return new BusinessProcessFormModelHandler( new TestFieldManager() );
                                                                     }
                                                                     return new TaskFormModelHandler( new TestFieldManager() );
                                                                 },
                                                                 new BPMNFormModelGeneratorImpl(),
                                                                 formSerializer,
                                                                 new StaticFormLayoutTemplateGenerator() );
    }

    @Test
    public void testBasicFuncionallities() throws Exception {
        assertEquals( "Form extension must be 'frm'", "frm", builderService.getFormExtension() );

        String formContent = builderService.buildEmptyFormContent( "fakeName" );

        assertNotNull( "Form content must not be null", formContent );

        FormDefinition form = formSerializer.deserialize( formContent );

        assertNotNull( "Form must not be null", form );
        assertNull( "FormModel must be null", form.getModel() );
        assertEquals( "Form should not have fields", 0, form.getFields().size() );
        assertNotNull( "Form layout must not be null", form.getLayoutTemplate() );
    }

    @Test
    public void testCreateNonExistingProcessForm() throws Exception {
        testCreateProcessForm( false );
    }

    @Test
    public void testCreateExistingProcessForm() throws Exception {
        testCreateProcessForm( true );
    }

    protected void testCreateProcessForm( boolean exists ) throws Exception {
        when( ioService.exists( any() ) ).thenReturn( exists );
        when( ioService.readAllString( any() ) ).thenReturn( IOUtils.toString( BPMNKieWorkbenchFormBuilderServiceTest.class.getResourceAsStream( "processForm.json" ) ) );

        String formConent = builderService.buildFormContent( formPath, definitions, null );

        assertNotNull( "Form Content cannot be null", formConent );

        FormDefinition form = formSerializer.deserialize( formConent );

        assertNotNull( "Form cannot be null", form );
        assertNotNull( "FormModel cannot be null", form.getModel() );
        assertTrue( "FormModel must be a BusinessProcessFormModel", form.getModel() instanceof BusinessProcessFormModel );
        assertNotNull( "Layout must be null", form.getLayoutTemplate() );
    }


    @Test
    public void testCreateNonExistingTaskForm() throws Exception {
        testCreateTaskForm( false );
    }

    @Test
    public void testCreateExistingTaskForm() throws Exception {
        testCreateTaskForm( true );
    }

    protected void testCreateTaskForm( boolean exists ) throws Exception {
        when( ioService.exists( any() ) ).thenReturn( exists );
        when( ioService.readAllString( any() ) ).thenReturn( IOUtils.toString( BPMNKieWorkbenchFormBuilderServiceTest.class.getResourceAsStream( "taskForm.json" ) ) );

        String formConent = builderService.buildFormContent( formPath, definitions, "_E57BCFF4-0787-40FA-83AC-627F06BF8F36" );

        assertNotNull( "Form Content cannot be null", formConent );

        FormDefinition form = formSerializer.deserialize( formConent );

        assertNotNull( "Form cannot be null", form );
        assertNotNull( "FormModel cannot be null", form.getModel() );
        assertTrue( "FormModel must be a TaskFormModel", form.getModel() instanceof TaskFormModel );
        assertNotNull( "Layout must be null", form.getLayoutTemplate() );
    }
}
