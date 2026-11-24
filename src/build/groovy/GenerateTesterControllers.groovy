import org.apache.maven.project.MavenProject

//@Field MavenProject project = binding.variables['project'] as MavenProject
MavenProject getProject() { binding.variables['project'] as MavenProject }

def specs = [
        [suffix: 'DFA', path: '/tester/dfa', ceout: 'AcceptorResponseDTO', imports: ['de.learnlib.benchmarking.dto.AcceptorResponseDTO']],
        [suffix: 'Mealy', path: '/tester/mealy', ceout: 'TransducerResponseDTO', imports: ['de.learnlib.benchmarking.dto.TransducerResponseDTO']],
]

def baseFile = new File(project.build.sourceDirectory as String, '/de/learnlib/benchmarking/api/TesterEndpoint.java')
def outDir = new File(project.build.directory as String, 'generated-sources/controllers/de/learnlib/benchmarking/api/')
outDir.mkdirs()

def baseCode = baseFile.getText('UTF-8')

specs.each { spec ->
    def generated = baseCode
            .replace('public interface TesterEndpoint<CEOUT>',
                    """${spec.imports.collect { "import ${it};" }.join('\\n')}
                       @io.avaje.http.api.Path("${spec.path}")
                       public interface TesterEndpoint${spec.suffix} extends TesterEndpoint<CEOUT>""")
            .replace('CEOUT', "${spec.ceout}")

    new File(outDir, "TesterEndpoint${spec.suffix}.java").setText(generated, 'UTF-8')
}
