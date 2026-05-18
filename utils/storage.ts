import * as FileSystem from 'expo-file-system';
import { Job, Client } from '@/types';

const DATA_DIR = `${FileSystem.documentDirectory}data/`;
const JOBS_FILE = `${DATA_DIR}jobs.json`;
const CLIENTS_FILE = `${DATA_DIR}clients.json`;

export async function ensureDataDir() {
  const dirInfo = await FileSystem.getInfoAsync(DATA_DIR);
  if (!dirInfo.exists) {
    await FileSystem.makeDirectoryAsync(DATA_DIR, { intermediates: true });
  }
}

export async function saveJobs(jobs: Job[]) {
  try {
    await ensureDataDir();
    await FileSystem.writeAsStringAsync(JOBS_FILE, JSON.stringify(jobs), {
      encoding: FileSystem.EncodingType.UTF8,
    });
  } catch (error) {
    console.error('Failed to save jobs:', error);
  }
}

export async function loadJobs(): Promise<Job[]> {
  try {
    await ensureDataDir();
    const info = await FileSystem.getInfoAsync(JOBS_FILE);
    if (info.exists) {
      const content = await FileSystem.readAsStringAsync(JOBS_FILE);
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
    await FileSystem.writeAsStringAsync(CLIENTS_FILE, JSON.stringify(clients), {
      encoding: FileSystem.EncodingType.UTF8,
    });
  } catch (error) {
    console.error('Failed to save clients:', error);
  }
}

export async function loadClients(): Promise<Client[]> {
  try {
    await ensureDataDir();
    const info = await FileSystem.getInfoAsync(CLIENTS_FILE);
    if (info.exists) {
      const content = await FileSystem.readAsStringAsync(CLIENTS_FILE);
      return JSON.parse(content);
    }
  } catch (error) {
    console.error('Failed to load clients:', error);
  }
  return [];
}

export async function exportAllData(jobs: Job[], clients: Client[]): Promise<string> {
  const data = JSON.stringify({ jobs, clients, exportedAt: new Date().toISOString() }, null, 2);
  const exportPath = `${FileSystem.cacheDirectory}forgetrack_export_${Date.now()}.json`;
  await FileSystem.writeAsStringAsync(exportPath, data);
  return exportPath;
}

export async function saveSignature(signatureData: string, jobId: string): Promise<string> {
  const dir = `${FileSystem.documentDirectory}signatures/`;
  await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  const path = `${dir}${jobId}_${Date.now()}.png`;
  await FileSystem.writeAsStringAsync(path, signatureData, {
    encoding: FileSystem.EncodingType.Base64,
  });
  return path;
}
