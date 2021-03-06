import sbt._

object ShellPrompt {

  object devnull extends ProcessLogger {
    def info(s: => String) {}

    def error(s: => String) {}

    def buffer[T](f: => T): T = f
  }

  val current = """\*\s+([\w-]+)""".r

  def currBranch =  
    try {
      (("git status -sb" lines_! devnull headOption)
      getOrElse "-" stripPrefix "## ")
      
    } catch {
      case t : Throwable => "NO-GIT"
    }
    

  val buildShellPrompt = {
    (state: State) => {
      val currProject = Project.extract(state).currentProject.id
      "%s:%s > ".format(
        currProject, currBranch
      )
    }
  }
}