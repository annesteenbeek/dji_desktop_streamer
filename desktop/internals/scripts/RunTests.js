import spawn from 'cross-spawn';
import path from 'path';

var pattern;
switch (process.argv[2]) {
  case 'dev':
    pattern = 'test/(?!e2e/)[^/]+/.+\\.spec\\.js$';
    // TODO fix default check, passing of argv
      // "./internals/scripts/CheckBuiltsExist.js" (included as setupFiles)
    break;
  case 'e2e':
    pattern = 'test/e2e/.+\\.spec\\.js';
    break;
  default:
    pattern = 'test/dev/.*\\.test\\.js';
}


const result = spawn.sync(
  path.normalize('./node_modules/.bin/jest'),
  [pattern, ...process.argv.slice(2)],
  { stdio: 'inherit' }
);

process.exit(result.status);
