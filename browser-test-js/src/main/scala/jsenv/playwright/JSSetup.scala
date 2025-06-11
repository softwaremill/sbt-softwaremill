package jsenv.playwright

import com.google.common.jimfs.Jimfs

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

private object JSSetup {
  def setupFile(enableCom: Boolean): Path = {
    val path = Jimfs.newFileSystem().getPath("setup.js")
    val contents = setupCode(enableCom).getBytes(StandardCharsets.UTF_8)
    Files.write(path, contents)
  }

  private def setupCode(enableCom: Boolean): String = {
    s"""
       |(function() {
       |  // Buffers for console.log / console.error
       |  var consoleLog = [];
       |  var consoleError = [];
       |
       |  // Buffer for errors.
       |  var errors = [];
       |
       |  // Buffer for outgoing messages.
       |  var outMessages = [];
       |
       |  // Buffer for incoming messages (used if onMessage not initalized).
       |  var inMessages = [];
       |
       |  // Callback for incoming messages.
       |  var onMessage = null;
       |
       |  function captureConsole(fun, buf) {
       |    if (!fun) return fun;
       |    return function() {
       |      var strs = []
       |      for (var i = 0; i < arguments.length; ++i)
       |        strs.push(String(arguments[i]));
       |
       |      buf.push(strs.join(" "));
       |      return fun.apply(this, arguments);
       |    }
       |  }
       |
       |  console.log = captureConsole(console.log, consoleLog);
       |  console.error = captureConsole(console.error, consoleError);
       |
       |  window.addEventListener('error', function(e) {
       |    errors.push(e.message)
       |  });
       |
       |  if ($enableCom) {
       |    this.scalajsCom = {
       |      init: function(onMsg) {
       |        onMessage = onMsg;
       |        window.setTimeout(function() {
       |          for (var m in inMessages)
       |            onMessage(inMessages[m]);
       |          inMessages = null;
       |        });
       |      },
       |      send: function(msg) { outMessages.push(msg); }
       |    }
       |  }
       |
       |  this.scalajsPlayWrightInternalInterface = {
       |    fetch: function() {
       |      var res = {
       |        consoleLog: consoleLog.slice(),
       |        consoleError: consoleError.slice(),
       |        errors: errors.slice(),
       |        msgs: outMessages.slice()
       |      }
       |
       |      consoleLog.length = 0;
       |      consoleError.length = 0;
       |      errors.length = 0;
       |      outMessages.length = 0;
       |
       |      return res;
       |    },
       |    send: function(msg) {
       |      if (inMessages !== null) inMessages.push(msg);
       |      else onMessage(msg);
       |    }
       |  };
       |}).call(this)
    """.stripMargin
  }

}
