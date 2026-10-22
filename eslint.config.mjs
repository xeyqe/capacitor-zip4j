import ionic from '@ionic/eslint-config/recommended.js';

export default [
  { ignores: ['dist/**', 'node_modules/**', 'example/node_modules/**', 'example/dist/**', 'android/**', 'ios/**'] },
  ...ionic,
];
