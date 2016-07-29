# Play jnotify binary

This is a repackaging of jnotify.

The reason this exists is that Play used to use a jnotify binary that was published to one of the old Typesafe repositories.  I don't know where this binary came from, I assume it was created years ago for Play 1, but it was different to the jar file that came with jnotify itself, as it had the native libraries packaged inside it.

When the Typesafe repositories were closed and we migrated to bintray, I went to find where this library had come from, and I couldn't.  So, rather than rely on a binary with no history, I created this project that rebuilds the jar we depend on from the offical jnotify distribution downloaded from sourceforge.

Originally, this was published to bintray.  Later, when we implemented maven support in Lagom, we needed it published to maven central.  At this point we changed the group id of the project to `com.lightbend.play`, because we don't have permission to deploy to the original group id of `net.contentobjects.jnotify` on maven central.
