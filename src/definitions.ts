export interface Zip4JPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
