import { WebPlugin } from '@capacitor/core';

import type { Zip4JPlugin } from './definitions';

export class Zip4JWeb extends WebPlugin implements Zip4JPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
