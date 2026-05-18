import { File, Directory, Paths } from 'expo-file-system';
import { Job, Client } from '@/types';

const dataDir = new Directory(Paths.document, 'data');
const jobsFile = new File(dataDir, 'jobs.json');
const clientsFile = new File(dataDir, 'clients.json');

export async function ensureDataDir() {
  try {
    dataDir.create();
  } catch {
    // Directory may already exist
  }
}

export async function saveJobs(jobs: Job[]) {
  try {
    await ensureDataDir();
    await jobsFile.write(JSON.stringify(jobs), { encoding: 'utf8' });
  } catch (error) {
    console.error('Failed to save jobs:', error);
  }
}

export async function loadJobs(): Promise<Job[]> {
  try {
    await ensureDataDir();
    if (jobsFile.exists) {
      const content = await jobsFile.text();
      return JSON.parse(content);
    }
  } catch (error) {
    console.error('Failed to load jobs:', error);
  }
  return [];
}

export async function saveClients(clients: Client[]) {
  try {
    await ensureDataDir();
    await clientsFile.write(JSON.stringify(clients), { encoding: 'utf8' });
  } catch (error) {
    console.error('Failed to save clients:', error);
  }
}

export async function loadClients(): Promise<Client[]> {
  try {
    await ensureDataDir();
    if (clientsFile.exists) {
      const content = await clientsFile.text();
      return JSON.parse(content);
    }
  } catch (error) {
    console.error('Failed to load clients:', error);
  }
  return [];
}

export async function exportAllData(jobs: Job[], clients: Client[]): Promise<string> {
  const data = JSON.stringify({ jobs, clients, exportedAt: new Date().toISOString() }, null, 2);
  const exportFile = new File(Paths.cache, `forgetrack_export_${Date.now()}.json`);
  await exportFile.write(data, { encoding: 'utf8' });
  return exportFile.uri;
}

export async function saveSignature(signatureData: string, jobId: string): Promise<string> {
  const sigDir = new Directory(Paths.document, 'signatures');
  sigDir.create();
  const sigFile = new File(sigDir, `${jobId}_${Date.now()}.png`);
  // Write base64 data
  const binary = atob(signatureData);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  await sigFile.write(bytes);
  return sigFile.uri;
}
