import { WebPlugin } from '@capacitor/core';

import type { Zip4JPlugin } from './definitions';

export class Zip4JWeb extends WebPlugin implements Zip4JPlugin {
  unzip(options: { source: string, destination: string, password?: string }): Promise<{ message: string }> {
    console.log('UNZIP', options);
    return Promise.resolve({message: 'UNZIP'});
  }
}
