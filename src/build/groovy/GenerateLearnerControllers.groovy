import org.apache.maven.project.MavenProject

//@Field MavenProject project = binding.variables['project'] as MavenProject
MavenProject getProject() { binding.variables['project'] as MavenProject }

def specs = [
        [suffix: 'DFA', path: '/learner/dfa', mqout: 'java.util.List<Boolean>', ceout: 'AcceptorResponseDTO', imports: ['de.learnlib.benchmarking.dto.AcceptorResponseDTO']],
        [suffix: 'Mealy', path: '/learner/mealy', mqout: 'java.util.List<java.util.List<String>>', ceout: 'TransducerResponseDTO', imports: ['de.learnlib.benchmarking.dto.TransducerResponseDTO']],
]

def baseFile = new File(project.build.sourceDirectory as String, '/de/learnlib/benchmarking/api/LearnerEndpoint.java')
def outDir = new File(project.build.directory as String, 'generated-sources/controllers/de/learnlib/benchmarking/api/')
outDir.mkdirs()

def baseCode = baseFile.getText('UTF-8')

specs.each { spec ->
    def generated = baseCode
            .replace('public interface LearnerEndpoint<MQOUT, CEOUT>',
                    """${spec.imports.collect { "import ${it};" }.join('\\n')}
                       @io.avaje.http.api.Path("${spec.path}")
                       public interface LearnerEndpoint${spec.suffix} extends LearnerEndpoint<MQOUT, CEOUT>""")
            .replace('MQOUT', "${spec.mqout}")
            .replace('CEOUT', "${spec.ceout}")

    new File(outDir, "LearnerEndpoint${spec.suffix}.java").setText(generated, 'UTF-8')
}
