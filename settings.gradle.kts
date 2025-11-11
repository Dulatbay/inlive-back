rootProject.name = "inlive"

include("client-libs:inlive-file-manager")
findProject(":client-libs:inlive-file-manager")?.name = "inlive-file-manager"