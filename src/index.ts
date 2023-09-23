import { registerPlugin } from '@capacitor/core';

import type { Zip4JPlugin } from './definitions';

const Zip4J = registerPlugin<Zip4JPlugin>('Zip4J', {
  web: () => import('./web').then(m => new m.Zip4JWeb()),
});

export * from './definitions';
export { Zip4J };
